package se.lth.cs.tycho.interp;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import se.lth.cs.tycho.analyze.memory.VariableInitOrderTransformer;
import se.lth.cs.tycho.analyze.util.AbstractBasicTraverser;
import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.interp.preprocess.EvaluateLiteralsTransformer;
import se.lth.cs.tycho.interp.preprocess.VariableOffsetTransformer;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.ConditionVisitor;
import se.lth.cs.tycho.ir.entity.am.ICall;
import se.lth.cs.tycho.ir.entity.am.ITest;
import se.lth.cs.tycho.ir.entity.am.IWait;
import se.lth.cs.tycho.ir.entity.am.Instruction;
import se.lth.cs.tycho.ir.entity.am.InstructionVisitor;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.parser.SourceCodeOracle;
import se.lth.cs.tycho.transform.caltoam.ActorStates;
import se.lth.cs.tycho.transform.caltoam.ActorToActorMachine;
import se.lth.cs.tycho.transform.filter.PrioritizeCallInstructions;
import se.lth.cs.tycho.transform.filter.SelectRandomInstruction;
import se.lth.cs.tycho.transform.operators.ActorOpTransformer;
import se.lth.cs.tycho.transform.util.StateHandler;

public class BasicActorMachineSimulator implements Simulator, InstructionVisitor<Integer, Environment>,
		ConditionVisitor<Boolean, Environment> {

	private final ActorMachine actorMachine;
	private final Environment environment;
	private final Interpreter interpreter;
	private final BitSet liveScopes;            // true if all variables in the scope is initialized, i.e. assigned to default values.
	private final Map<Condition, BitSet> condRequiredScope;   // for each actor machine condition, which scopes are required
	private final BitSet[] transRequiredScope;  // for each transition, which scopes are required
	
	private final TypeConverter converter;

	private int state;

	/**
	 * Transform an Actor to an ActorMachine which is prepared for interpretation, i.e. variables are ordered in initialization order, 
	 * variable and port offsets are computed, operations are replaced by function calls et.c.
	 * 
	 * @param actor
	 * @return an ActorMachine ready to be simulated by BasicActorMachineSimulator.
	 * @throws CALCompiletimeException if an error occurred
	 */
	public static ActorMachine prepareActor(Actor actor, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		// Order variable declarations so they can be initialized in declaration order
		actor = VariableInitOrderTransformer.transformActor(actor, sourceOracle);
		// replace BinOp and UnaryOp in all expressions with function calls
		actor = ActorOpTransformer.transformActor(actor, sourceOracle);

		// translate the actor to an actor machine
		ActorToActorMachine trans = new ActorToActorMachine() {
			@Override
			protected StateHandler<ActorStates.State> getStateHandler(StateHandler<ActorStates.State> stateHandler) {
				stateHandler = new PrioritizeCallInstructions<>(stateHandler);
				stateHandler = new SelectRandomInstruction<>(stateHandler);
				return stateHandler;
			}
		};
		ActorMachine actorMachine = trans.translate(actor);
		
		actorMachine = BasicActorMachineSimulator.prepareActorMachine(actorMachine, sourceOracle);

		return actorMachine;
	}
	
	public static ActorMachine prepareActorMachine(ActorMachine actorMachine, SourceCodeOracle sourceOracle){
		// replace ExprLiteral with ExprValue. This removes the global variables for predefined functions, i.e. $BinaryOperation.+ 
		actorMachine = EvaluateLiteralsTransformer.transformActorMachine(actorMachine, sourceOracle);
		// memory layout (stack offset)
		actorMachine = VariableOffsetTransformer.transformActorMachine(actorMachine, sourceOracle);

		return actorMachine;
	}
	
	public BasicActorMachineSimulator(ActorMachine actorMachine, Environment environment, Interpreter interpreter) {
		this.actorMachine = actorMachine;
		this.environment = environment;
		this.interpreter = interpreter;
		this.converter = TypeConverter.getInstance();
		this.state = 0;
		ImmutableList<Scope> scopeList = actorMachine.getScopes();
		int nbrScopes = scopeList.size();
		this.liveScopes = new BitSet(nbrScopes);
		// conditions, find the required scopes for each condition
		condRequiredScope = new HashMap<Condition, BitSet>(actorMachine.getConditions().size());
		FindRequiredScopes exprTraveler = new FindRequiredScopes();
		for(Condition cond : actorMachine.getConditions()){
			BitSet req = new BitSet(nbrScopes);
			if(cond.kind() == Condition.ConditionKind.predicate){
				Expression expr = ((PredicateCondition)cond).getExpression();
				expr.accept(exprTraveler, req);
				condRequiredScope.put(cond, req);
			}
		}
		// transitions, find the required scopes for each transition
		transRequiredScope = new BitSet[actorMachine.getTransitions().size()];
		for(int i=0; i<transRequiredScope.length; i++){
			Transition trans = actorMachine.getTransition(i);
			transRequiredScope[i] = new BitSet(nbrScopes);
			trans.getBody().accept(exprTraveler, transRequiredScope[i]);
		}
		
		// Check that all input and output ports are connected to channels
		assert actorMachine.getInputPorts().size() == environment.getSourceChannelOutputEnds().length;
		assert actorMachine.getOutputPorts().size() == environment.getSinkChannelInputEnds().length;
	}
	
	@Override
	public boolean step() {
		boolean done = false;
		Instruction i = null;
		while (!done) {
			i = actorMachine.getInstructions(state).get(0);
			state = i.accept(this, environment);
			assert interpreter.getStack().isEmpty();
			if (i instanceof ICall || i instanceof IWait) {
				done = true;
			}
		}
		return i instanceof ICall;
	}

	private class FindRequiredScopes extends AbstractBasicTraverser<BitSet>{
		@Override
		public void traverseVariable(Variable var, BitSet p) {
			if(var.isScopeVariable()){
				p.set(var.getScopeId());
			}
		}
	}

	
	private void initScopes(BitSet required) {
		ImmutableList<Scope> scopeList = actorMachine.getScopes();
		int nbrScopes = scopeList.size();
		//FIXME, scopes may depend on each other, ensure a correct evaluation order. The order 0..N is likely ok since the actor scope is initialized first.
		//if the scopes are ordered Actor ..local this code works fine.
		for(int scopeId=0; scopeId<nbrScopes; scopeId++){
			if(required.get(scopeId) && !liveScopes.get(scopeId)){
				ImmutableList<LocalVarDecl> declList = scopeList.get(scopeId).getDeclarations();
				//FIXME, find a correct evaluation order, variables can be dependent on each other.
				for(int declOffset=0; declOffset<declList.size() ; declOffset++){
					Ref memCell = environment.getMemory().declare(scopeId, declOffset);
					Expression initExpr = declList.get(declOffset).getInitialValue();
					if(initExpr != null){
						interpreter.evaluate(initExpr, environment).assignTo(memCell);
					}
				}
				liveScopes.set(scopeId);
			}
		}
	}

	@Override
	public Integer visitWait(IWait i, Environment p) {
		return i.S();
	}

	@Override
	public Integer visitTest(ITest i, Environment p) {
		Condition condExpr = actorMachine.getCondition(i.C());
		boolean cond = condExpr.accept(this, p);
		return cond ? i.S1() : i.S0();
	}

	@Override
	public Integer visitCall(ICall i, Environment p) {
		Transition trans = actorMachine.getTransition(i.T());
		initScopes(transRequiredScope[i.T()]);
		interpreter.execute(trans.getBody(), p);

		//FIXME, kill only the scopes in transaction.kill
		boolean actorScopeIsLive = liveScopes.get(0);
		liveScopes.clear();
		if(actorScopeIsLive) { liveScopes.set(0); }
		return i.S();
	}

	@Override
	public Boolean visitInputCondition(PortCondition c, Environment p) {
		int id = c.getPortName().getOffset();
		Channel.OutputEnd channel = environment.getSourceChannelOutputEnd(id);
		return channel.tokens(c.N());
	}

	@Override
	public Boolean visitOutputCondition(PortCondition c, Environment p) {
		int id = c.getPortName().getOffset();
		Channel.InputEnd channel = environment.getSinkChannelInputEnd(id);
		return channel.space(c.N());
	}

	@Override
	public Boolean visitPredicateCondition(PredicateCondition c, Environment p) {
		initScopes(condRequiredScope.get(c));
		RefView cond = interpreter.evaluate(c.getExpression(), p);
		return converter.getBoolean(cond);
	}

	@Override
	public void scopesToString(StringBuffer sb){
		ImmutableList<Scope> scopeList = actorMachine.getScopes();
		for(int scopeId=0; scopeId<scopeList.size(); scopeId++){
			if(liveScopes.get(scopeId)){
				sb.append("{\n");
				ImmutableList<LocalVarDecl> declList = scopeList.get(scopeId).getDeclarations();
				for(int declId=0; declId<declList.size(); declId++){
					VariableLocation var = VariableLocation.scopeVariable(actorMachine, declList.get(declId).getName(), scopeId, declId);
					sb.append("  " + var.getName() + " : ");
					sb.append(environment.getMemory().get(var).toString() + "\n");
				}
				sb.append("}\n");
			}
		}
	}

}
