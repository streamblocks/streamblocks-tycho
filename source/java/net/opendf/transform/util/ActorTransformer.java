package net.opendf.transform.util;

import net.opendf.ir.cal.Transition;
import net.opendf.ir.cal.Action;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.cal.InputPattern;
import net.opendf.ir.cal.OutputExpression;
import net.opendf.ir.cal.ScheduleFSM;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.QID;
import net.opendf.ir.util.ImmutableList;

public interface ActorTransformer<P> extends BasicTransformer<P> {
	public Actor transformActor(Actor actor, P param);

	public Action transformAction(Action action, P param);
	public ImmutableList<Action> transformActions(ImmutableList<Action> actions, P param);

	public InputPattern transformInputPattern(InputPattern input, P param);
	public ImmutableList<InputPattern> transformInputPatterns(ImmutableList<InputPattern> inputs, P param);

	public OutputExpression transformOutputExpression(OutputExpression output, P param);
	public ImmutableList<OutputExpression> transformOutputExpressions(ImmutableList<OutputExpression> output, P param);

	public ImmutableList<ImmutableList<QID>> transformPriorities(ImmutableList<ImmutableList<QID>> prios, P param);

	public ScheduleFSM transformSchedule(ScheduleFSM schedule, P param);

	public Transition transformScheduleTransition(Transition transition, P param);
	public ImmutableList<Transition> transformScheduleTransitions(ImmutableList<Transition> transitions, P param);
	
	public QID transformTag(QID tag, P param);
	public ImmutableList<QID> transformTags(ImmutableList<QID> tags, P param);
	
	public PortDecl transformInputPort(PortDecl port, P param);
	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> port, P param);

	public PortDecl transformOutputPort(PortDecl port, P param);
	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> port, P param);
}
