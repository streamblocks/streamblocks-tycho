package se.lth.cs.tycho.transform.util;

import java.lang.invoke.MethodHandle;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.ProcessDescription;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class AbstractActorTransformer<P> extends AbstractBasicTransformer<P> implements ActorTransformer<P> {

	private static final MethodHandle transAction = methodHandle(AbstractActorTransformer.class, Action.class, "transformAction");
	private static final MethodHandle transInputPattern = methodHandle(AbstractActorTransformer.class, InputPattern.class, "transformInputPattern");
	private static final MethodHandle transOutputExpression = methodHandle(AbstractActorTransformer.class,
			OutputExpression.class, "transformOutputExpression");
	private static final MethodHandle transTransition = methodHandle(AbstractActorTransformer.class, Transition.class, "transformScheduleTransition");
	private static final MethodHandle transQID = methodHandle(AbstractActorTransformer.class, QID.class, "transformTag");
	private static final MethodHandle transInputPort = methodHandle(AbstractActorTransformer.class, PortDecl.class, "transformInputPort");
	private static final MethodHandle transOutputPort = methodHandle(AbstractActorTransformer.class, PortDecl.class, "transformOutputPort");

	@Override
	public CalActor transformActor(CalActor calActor, P param) {
		return calActor.copy(
				transformTypeParameters(calActor.getTypeParameters(), param),
				transformValueParameters(calActor.getValueParameters(), param),
				transformTypeDecls(calActor.getTypeDecls(), param),
				transformVarDecls(calActor.getVarDecls(), param),
				transformInputPorts(calActor.getInputPorts(), param),
				transformOutputPorts(calActor.getOutputPorts(), param),
				transformActions(calActor.getInitializers(), param),
				transformActions(calActor.getActions(), param),
				transformSchedule(calActor.getScheduleFSM(), param),
				transformProcessDescription(calActor.getProcessDescription(), param),
				transformPriorities(calActor.getPriorities(), param),
				transformExpressions(calActor.getInvariants(), param));
	}

	@Override
	public Action transformAction(Action action, P param) {
		return action.copy(
				transformTag(action.getTag(), param),
				transformInputPatterns(action.getInputPatterns(), param),
				transformOutputExpressions(action.getOutputExpressions(), param),
				transformTypeDecls(action.getTypeDecls(), param),
				transformVarDecls(action.getVarDecls(), param),
				transformExpressions(action.getGuards(), param),
				transformStatements(action.getBody(), param),
				transformExpression(action.getDelay(), param),
				transformExpressions(action.getPreconditions(), param),
				transformExpressions(action.getPostconditions(), param));
	}

	@Override
	public ImmutableList<Action> transformActions(ImmutableList<Action> actions, P param) {
		return transformList(transAction, actions, param);
	}

	@Override
	public ImmutableList<ImmutableList<QID>> transformPriorities(ImmutableList<ImmutableList<QID>> prios, P param) {
		if (prios == null) {
			return null;
		}
		ImmutableList.Builder<ImmutableList<QID>> builder = ImmutableList.builder();
		for (ImmutableList<QID> prio : prios) {
			builder.add(transformList(transQID, prio, param));
		}
		return builder.build();
	}

	@Override
	public ScheduleFSM transformSchedule(ScheduleFSM schedule, P param) {
		if (schedule == null){
			return null;
		}
		return schedule
				.copy(transformScheduleTransitions(schedule.getTransitions(), param), schedule.getInitialState());
	}

	@Override
	public ProcessDescription transformProcessDescription(ProcessDescription process, P param) {
		if (process == null) {
			return null;
		} else {
			return process.copy(transformStatements(process.getStatements(), param), process.isRepeated());
		}
	}

	@Override
	public Transition transformScheduleTransition(Transition transition, P param) {
		return transition.copy(
				transition.getSourceState(),
				transition.getDestinationState(),
				transformTags(transition.getActionTags(), param));
	}

	@Override
	public ImmutableList<Transition> transformScheduleTransitions(ImmutableList<Transition> transitions, P param) {
		return transformList(transTransition, transitions, param);
	}

	@Override
	public InputPattern transformInputPattern(InputPattern input, P param) {
		return input.copy(
				transformPort(input.getPort(), param),
				input.getVariables(),
				transformExpression(input.getRepeatExpr(), param));
	}

	@Override
	public ImmutableList<InputPattern> transformInputPatterns(ImmutableList<InputPattern> inputs, P param) {
		return transformList(transInputPattern, inputs, param);
	}

	@Override
	public QID transformTag(QID tag, P param) {
		return tag;
	}

	@Override
	public ImmutableList<QID> transformTags(ImmutableList<QID> tags, P param) {
		return transformList(transQID, tags, param);
	}

	@Override
	public PortDecl transformInputPort(PortDecl port, P param) {
		return port.copy(port.getName(), transformTypeExpr(port.getType(), param));
	}

	@Override
	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> port, P param) {
		return transformList(transInputPort, port, param);
	}

	@Override
	public PortDecl transformOutputPort(PortDecl port, P param) {
		return port.copy(port.getName(), transformTypeExpr(port.getType(), param));
	}

	@Override
	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> port, P param) {
		return transformList(transOutputPort, port, param);
	}

	@Override
	public OutputExpression transformOutputExpression(OutputExpression output, P param) {
		return output.copy(
				transformPort(output.getPort(), param),
				transformExpressions(output.getExpressions(), param),
				transformExpression(output.getRepeatExpr(), param));
	}

	@Override
	public ImmutableList<OutputExpression> transformOutputExpressions(ImmutableList<OutputExpression> output, P param) {
		return transformList(transOutputExpression, output, param);
	}

}
