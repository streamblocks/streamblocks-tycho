package net.opendf.analyze.util;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.State;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.util.ImmutableList;

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
