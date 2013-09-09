package net.opendf.analyze.util;

import net.opendf.ir.cal.Transition;
import net.opendf.ir.cal.Action;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.cal.InputPattern;
import net.opendf.ir.cal.OutputExpression;
import net.opendf.ir.cal.ScheduleFSM;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.QID;
import net.opendf.ir.util.ImmutableList;

public abstract class AbstractActorTraverser<P> extends AbstractBasicTraverser<P> implements ActorTraverser<P> {

	@Override
	public void traverseActor(Actor actor, P param) {
		traverseTypeParameters(actor.getTypeParameters(), param);
		traverseValueParameters(actor.getValueParameters(), param);
		traverseInputPorts(actor.getInputPorts(), param);
		traverseOutputPorts(actor.getOutputPorts(), param);
		traverseTypeDecls(actor.getTypeDecls(), param);
		traverseVarDecls(actor.getVarDecls(), param);
		traverseExpressions(actor.getInvariants(), param);
		traverseActions(actor.getInitializers(), param);
		traverseActions(actor.getActions(), param);
		traverseSchedule(actor.getScheduleFSM(), param);
		traversePriorities(actor.getPriorities(), param);
	}

	@Override
	public void traverseAction(Action action, P param) {
		traverseTag(action.getTag(), param);
		traverseInputPatterns(action.getInputPatterns(), param);
		traverseTypeDecls(action.getTypeDecls(), param);
		traverseVarDecls(action.getVarDecls(), param);
		traverseExpressions(action.getGuards(), param);
		traverseStatements(action.getBody(), param);
		traverseOutputExpressions(action.getOutputExpressions(), param);
		traverseExpression(action.getDelay(), param);
		traverseExpressions(action.getPreconditions(), param);
		traverseExpressions(action.getPostconditions(), param);
	}

	@Override
	public void traverseActions(ImmutableList<Action> actions, P param) {
		for (Action action : actions) {
			traverseAction(action, param);
		}
	}

	@Override
	public void traverseInputPattern(InputPattern input, P param) {
		traversePort(input.getPort(), param);
		traverseVarDecls(input.getVariables(), param);
		traverseExpression(input.getRepeatExpr(), param);
	}

	@Override
	public void traverseInputPatterns(ImmutableList<InputPattern> input, P param) {
		for (InputPattern in : input) {
			traverseInputPattern(in, param);
		}
	}

	@Override
	public void traverseOutputExpression(OutputExpression output, P param) {
		traversePort(output.getPort(), param);
		traverseExpressions(output.getExpressions(), param);
		traverseExpression(output.getRepeatExpr(), param);
	}

	@Override
	public void traverseOutputExpressions(ImmutableList<OutputExpression> output, P param) {
		for (OutputExpression out : output) {
			traverseOutputExpression(out, param);
		}
	}

	@Override
	public void traversePriorities(ImmutableList<ImmutableList<QID>> prios, P param) {
		for (ImmutableList<QID> prioSeq : prios) {
			traverseTags(prioSeq, param);
		}
	}

	@Override
	public void traverseSchedule(ScheduleFSM schedule, P param) {
		traverseScheduleTransitions(schedule.getTransitions(), param);
	}

	@Override
	public void traverseScheduleTransition(Transition transition, P param) {
		traverseTags(transition.getActionTags(), param);
	}

	@Override
	public void traverseScheduleTransitions(ImmutableList<Transition> transitions, P param) {
		for (Transition transition : transitions) {
			traverseScheduleTransition(transition, param);
		}
	}

	@Override
	public void traverseTag(QID tag, P param) {
	}

	@Override
	public void traverseTags(ImmutableList<QID> tags, P param) {
		for (QID tag : tags) {
			traverseTag(tag, param);
		}
	}

	@Override
	public void traverseInputPort(PortDecl port, P param) {
		traverseTypeExpr(port.getType(), param);
	}

	@Override
	public void traverseInputPorts(ImmutableList<PortDecl> port, P param) {
		for (PortDecl p : port) {
			traverseInputPort(p, param);
		}
	}

	@Override
	public void traverseOutputPort(PortDecl port, P param) {
		traverseTypeExpr(port.getType(), param);
	}

	@Override
	public void traverseOutputPorts(ImmutableList<PortDecl> port, P param) {
		for (PortDecl p : port) {
			traverseOutputPort(p, param);
		}
	}

}
