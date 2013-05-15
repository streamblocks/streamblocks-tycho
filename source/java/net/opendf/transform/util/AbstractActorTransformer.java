package net.opendf.transform.util;

import java.lang.invoke.MethodHandle;

import net.opendf.ir.cal.Action;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.cal.InputPattern;
import net.opendf.ir.cal.OutputExpression;
import net.opendf.ir.cal.ScheduleFSM;
import net.opendf.ir.cal.Transition;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.QID;
import net.opendf.ir.util.ImmutableList;

public class AbstractActorTransformer<P> extends AbstractBasicTransformer<P> implements ActorTransformer<P> {

	private static final MethodHandle transAction = methodHandle(Action.class, "transformAction");
	private static final MethodHandle transInputPattern = methodHandle(InputPattern.class, "transformInputPattern");
	private static final MethodHandle transOutputExpression = methodHandle(OutputExpression.class,
			"transformOutputExpression");
	private static final MethodHandle transTransition = methodHandle(Transition.class, "transformScheduleTransition");
	private static final MethodHandle transQID = methodHandle(QID.class, "transformTag");
	private static final MethodHandle transInputPort = methodHandle(PortDecl.class, "transformInputPort");
	private static final MethodHandle transOutputPort = methodHandle(PortDecl.class, "transformOutputPort");

	@Override
	public Actor transformActor(Actor actor, P param) {
		return actor.copy(
				actor.getName(),
				actor.getNamespaceDecl(),
				transformTypeParameters(actor.getTypeParameters(), param),
				transformValueParameters(actor.getValueParameters(), param),
				transformTypeDecls(actor.getTypeDecls(), param),
				transformVarDecls(actor.getVarDecls(), param),
				transformInputPorts(actor.getInputPorts(), param),
				transformOutputPorts(actor.getOutputPorts(), param),
				transformActions(actor.getInitializers(), param),
				transformActions(actor.getActions(), param),
				transformSchedule(actor.getScheduleFSM(), param),
				transformPriorities(actor.getPriorities(), param),
				transformExpressions(actor.getInvariants(), param));
	}

	@Override
	public Action transformAction(Action action, P param) {
		return action.copy(
				action.getID(),
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
