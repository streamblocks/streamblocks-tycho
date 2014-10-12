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

public interface ActorTraverser<P> extends BasicTraverser<P> {
	public void traverseActor(CalActor calActor, P param);

	public void traverseAction(Action action, P param);

	public void traverseActions(ImmutableList<Action> actions, P param);

	public void traverseInputPattern(InputPattern input, P param);

	public void traverseInputPatterns(ImmutableList<InputPattern> inputs, P param);

	public void traverseOutputExpression(OutputExpression output, P param);

	public void traverseOutputExpressions(ImmutableList<OutputExpression> output, P param);

	public void traversePriorities(ImmutableList<ImmutableList<QID>> prios, P param);

	public void traverseSchedule(ScheduleFSM schedule, P param);

	public void traverseScheduleTransition(Transition transition, P param);

	public void traverseScheduleTransitions(ImmutableList<Transition> transitions, P param);

	public void traverseTag(QID tag, P param);

	public void traverseTags(ImmutableList<QID> tags, P param);

	public void traverseInputPort(PortDecl port, P param);

	public void traverseInputPorts(ImmutableList<PortDecl> port, P param);

	public void traverseOutputPort(PortDecl port, P param);

	public void traverseOutputPorts(ImmutableList<PortDecl> port, P param);

}
