package se.lth.cs.tycho.analyze.util;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.util.ImmutableList;

public abstract class AbstractActorTraverser<P> extends AbstractBasicTraverser<P> implements ActorTraverser<P> {

	@Override
	public void traverseActor(CalActor calActor, P param) {
		traverseTypeParameters(calActor.getTypeParameters(), param);
		traverseValueParameters(calActor.getValueParameters(), param);
		traverseInputPorts(calActor.getInputPorts(), param);
		traverseOutputPorts(calActor.getOutputPorts(), param);
		traverseTypeDecls(calActor.getTypeDecls(), param);
		traverseVarDecls(calActor.getVarDecls(), param);
		traverseExpressions(calActor.getInvariants(), param);
		traverseActions(calActor.getInitializers(), param);
		traverseActions(calActor.getActions(), param);
		traverseSchedule(calActor.getScheduleFSM(), param);
		traversePriorities(calActor.getPriorities(), param);
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
