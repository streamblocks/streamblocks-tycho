package net.opendf.transform.caltoam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.opendf.ir.Port;
import net.opendf.ir.QID;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.entity.am.Condition;
import net.opendf.ir.entity.am.PortCondition;
import net.opendf.ir.entity.am.PredicateCondition;
import net.opendf.ir.entity.am.Scope;
import net.opendf.ir.entity.am.Transition;
import net.opendf.ir.entity.cal.Action;
import net.opendf.ir.entity.cal.Actor;
import net.opendf.ir.entity.cal.InputPattern;
import net.opendf.ir.entity.cal.OutputExpression;
import net.opendf.ir.entity.cal.ScheduleFSM;
import net.opendf.ir.expr.ExprLiteral;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.expr.ExprLiteral.Kind;
import net.opendf.ir.stmt.Statement;
import net.opendf.ir.stmt.StmtBlock;
import net.opendf.ir.stmt.StmtConsume;
import net.opendf.ir.stmt.StmtOutput;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.ImmutableList.Builder;
import net.opendf.transform.caltoam.util.QIDMap;

class ActorToActorMachineHelper {
	private static final ActorVariableExtractor ave = new ActorVariableExtractor();
	private static final AddNumberedPorts anp = new AddNumberedPorts();
	private final ActorVariableExtractor.Result aveResult;
	private QIDMap<Integer> qidMap;
	private PriorityHandler prioHandler;
	private ScheduleHandler schedHandler;
	private ImmutableList<Transition> transitions;
	private ImmutableList<Condition> conditions;
	private ConditionHandler condHandler;
	private List<String> stateList;

	public ActorToActorMachineHelper(Actor actor) {
		aveResult = ave.extractVariables(anp.addNumberedPorts(actor));
	}
	
	public ActorStateHandler getActorStateHandler() {
		ScheduleHandler scheduleHandler = getScheduleHandler();
		ActorStates actorStates = new ActorStates(getConditions(), getStateList(), scheduleHandler.initialState(), getActor().getInputPorts().size());
		return new ActorStateHandler(scheduleHandler, getConditionHandler(), getPriorityHandler(), getTransitions(), actorStates);
	}

	private Actor getActor() {
		return aveResult.actor;
	}
	
	public ImmutableList<PortDecl> getInputPorts() {
		return getActor().getInputPorts();
	}
	
	public ImmutableList<PortDecl> getOutputPorts() {
		return getActor().getOutputPorts();
	}
	
	public ImmutableList<Scope> getScopes() {
		return aveResult.scopes;
	}
	
	public ImmutableList<Condition> getConditions() {
		if (conditions == null) {
			createConditionsAndHandler();
		}
		return conditions;
	}
	
	private ConditionHandler getConditionHandler() {
		if (condHandler == null) {
			createConditionsAndHandler();
		}
		return condHandler;
	}
	
	private void createConditionsAndHandler() {
		ConditionHandler.Builder handler = new ConditionHandler.Builder();
		ImmutableList.Builder<Condition> conditions = ImmutableList.builder();
		ImmutableList<Action> actions = ImmutableList.<Action> builder()
				.addAll(getActor().getInitializers())
				.addAll(getActor().getActions())
				.build();
		int condNbr = 0;
		int actionNbr = 0;
		for (Action action : actions) {
			handler.addAction(actionNbr);
			int firstPortCond = condNbr;
			for (InputPattern input : action.getInputPatterns()) {
				PortCondition c = portCond(input);
				handler.addCondition(actionNbr, condNbr);
				conditions.add(c);
				condNbr += 1;
			}
			int firstPredCond = condNbr;
			for (Expression guard : action.getGuards()) {
				PredicateCondition c = new PredicateCondition(guard);
				handler.addCondition(actionNbr, condNbr);
				conditions.add(c);
				condNbr += 1;
			}
			for (int portCond = firstPortCond; portCond < firstPredCond; portCond++) {
				for (int predCond = firstPredCond; predCond < condNbr; predCond++) {
					handler.addDependency(portCond, predCond);
				}
			}
			actionNbr += 1;
		}
		this.condHandler = handler.build();
		this.conditions = conditions.build();
	}
	
	private PortCondition portCond(InputPattern input) {
		int vars = input.getVariables().size();
		int rep = getRepeatMultiplier(input.getRepeatExpr());
		return new PortCondition(input.getPort(), vars * rep);
	}
	
	

	public ImmutableList<Transition> getTransitions() {
		if (transitions == null) {
			ImmutableList.Builder<Transition> builder = ImmutableList.builder();
			int index = 0;
			for (Action action : getActor().getInitializers()) {
				builder.add(createTransition(index, action));
				index += 1;
			}
			for (Action action : getActor().getActions()) {
				builder.add(createTransition(index, action));
				index += 1;
			}
			transitions = builder.build();
		}
		return transitions;
	}

	private Transition createTransition(int index, Action action) {
		ImmutableList.Builder<Statement> builder = ImmutableList.builder();
		builder.addAll(action.getBody());
		addOutputStmts(builder, action.getOutputExpressions());
		addConsumeStmts(builder, action.getInputPatterns());
		StmtBlock body = new StmtBlock(null, null, builder.build());
		return new Transition(getInputRates(action.getInputPatterns()), getOutputRates(action.getOutputExpressions()),
				aveResult.transientScopes, body);
	}

	private void addConsumeStmts(Builder<Statement> builder, ImmutableList<InputPattern> inputPatterns) {
		for (InputPattern input : inputPatterns) {
			Port port = copyPort(input.getPort());
			int tokens = input.getVariables().size() * getRepeatMultiplier(input.getRepeatExpr());
			builder.add(new StmtConsume(port, tokens));
		}
	}

	private void addOutputStmts(ImmutableList.Builder<Statement> builder, ImmutableList<OutputExpression> outputExpressions) {
		for (OutputExpression output : outputExpressions) {
			if (output.getRepeatExpr() != null) {
				builder.add(new StmtOutput(output.getExpressions(), copyPort(output.getPort()), getRepeatMultiplier(output.getRepeatExpr())));
			} else {
				builder.add(new StmtOutput(output.getExpressions(), copyPort(output.getPort())));
			}
		}
	}

	private Port copyPort(Port port) {
		if (port.hasLocation()) {
			return new Port(port.getName(), port.getOffset());
		} else {
			return new Port(port.getName());
		}
	}
	
	private Map<Port, Integer> getOutputRates(ImmutableList<OutputExpression> outputExpressions) {
		Map<Port, Integer> outputRates = new HashMap<>();
		for (OutputExpression output : outputExpressions) {
			int vars = output.getExpressions().size();
			int rep = getRepeatMultiplier(output.getRepeatExpr());
			outputRates.put(copyPort(output.getPort()), vars * rep);
		}
		return outputRates;
	}

	private Map<Port, Integer> getInputRates(ImmutableList<InputPattern> inputPatterns) {
		Map<Port, Integer> inputRates = new HashMap<>();
		for (InputPattern input : inputPatterns) {
			int vars = input.getVariables().size();
			int rep = getRepeatMultiplier(input.getRepeatExpr());
			inputRates.put(copyPort(input.getPort()), vars * rep);
		}
		return inputRates;
	}

	private int getRepeatMultiplier(Expression repeat) {
		if (repeat == null)
			return 1;
		assert repeat instanceof ExprLiteral;
		ExprLiteral lit = (ExprLiteral) repeat;
		assert lit.getKind() == Kind.Integer;
		return Integer.parseInt(lit.getText());
	}

	private void createScheduleHandlerAndStateList() {
		Actor actor = getActor();
		ScheduleFSM schedule = actor.getScheduleFSM();
		int numInit = actor.getInitializers().size();
		int numAction = actor.getActions().size();
		String initState = schedule == null ? "" : schedule.getInitialState();
		ScheduleHandler.Builder builder = new ScheduleHandler.Builder(initState);
		for (int i = 0; i < numInit; i++) {
			builder.addInitAction(i);
		}
		if (schedule == null) {
			for (int a = numInit; a < numInit+numAction; a++) {
				builder.addTransition("", a, "");
			}
		} else {
			QIDMap<Integer> qidMap = getQIDMap();
			for (net.opendf.ir.entity.cal.Transition t : schedule.getTransitions()) {
				String source = t.getSourceState();
				String destination = t.getDestinationState();
				for (QID tag : t.getActionTags()) {
					for (int action : qidMap.get(tag)) {
						builder.addTransition(source, action, destination);
					}
				}
			}
			for (int action : qidMap.getTagLess()) {
				builder.addUnscheduled(action);
			}
		}
		schedHandler = builder.build();
		stateList = builder.stateList();
	}
	
	private List<String> getStateList() {
		if (stateList == null) {
			createScheduleHandlerAndStateList();
		}
		return stateList;
	}
	
	private ScheduleHandler getScheduleHandler() {
		if (schedHandler == null) {
			createScheduleHandlerAndStateList();
		}
		return schedHandler;
	}

	private PriorityHandler getPriorityHandler() {
		if (prioHandler == null) {
			PriorityHandler.Builder builder = new PriorityHandler.Builder(getTransitions().size());
			ImmutableList<ImmutableList<QID>> priorities = getActor().getPriorities();
			if (priorities != null) {
				for (ImmutableList<QID> prios : priorities) {
					Iterator<QID> iter = prios.iterator();
					QID hi = iter.next();
					while (iter.hasNext()) {
						QID lo = iter.next();
						addPrio(builder, hi, lo);
						hi = lo;
					}
				}
			}
			prioHandler = builder.build();
		}
		return prioHandler;
	}

	private void addPrio(PriorityHandler.Builder builder, QID high, QID low) {
		QIDMap<Integer> qidMap = getQIDMap();
		for (int hi : qidMap.get(high)) {
			for (int lo : qidMap.get(low)) {
				builder.addPriority(hi, lo);
			}
		}
	}

	private QIDMap<Integer> getQIDMap() {
		if (qidMap == null) {
			qidMap = new QIDMap<>();
			int id = getActor().getInitializers().size();
			for (Action action : getActor().getActions()) {
				qidMap.put(action.getTag(), id);
				id += 1;
			}
		}
		return qidMap;
	}
}
