package se.lth.cs.tycho.analyze.util;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public interface ActorMachineTraverser<P> extends BasicTraverser<P> {
	public void traverseActorMachine(ActorMachine actorMachine, P param);

	public void traverseScopes(ImmutableList<Scope> scopes, P param);

	public void traverseScope(Scope scope, P param);

	public void traverseTransitions(ImmutableList<Transition> transitions, P param);

	public void traverseTransition(Transition transition, P param);

	public void traverseConditions(ImmutableList<Condition> conditions, P param);

	public void traverseCondition(Condition condition, P param);

	public void traverseController(ImmutableList<State> controller, P param);

	public void traverseControllerState(State state, P param);

	public void traverseInstruction(Instruction instr, P param);

	public void traverseInputPorts(ImmutableList<PortDecl> inputPorts, P param);

	public void traverseInputPort(PortDecl inputPort, P param);

	public void traverseOutputPorts(ImmutableList<PortDecl> outputPorts, P param);

	public void traverseOutputPort(PortDecl outputPort, P param);

}
