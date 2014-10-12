package se.lth.cs.tycho.analyze.util;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.ConditionVisitor;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.InstructionVisitor;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public abstract class AbstractActorMachineTraverser<P> extends AbstractBasicTraverser<P> implements
		ActorMachineTraverser<P>,
		InstructionVisitor<Void, P>, ConditionVisitor<Void, P> {

	@Override
	public void traverseActorMachine(ActorMachine actorMachine, P param) {
		traverseInputPorts(actorMachine.getInputPorts(), param);
		traverseOutputPorts(actorMachine.getOutputPorts(), param);
		traverseScopes(actorMachine.getScopes(), param);
		traverseConditions(actorMachine.getConditions(), param);
		traverseTransitions(actorMachine.getTransitions(), param);
		traverseController(actorMachine.getController(), param);
	}

	@Override
	public void traverseScopes(ImmutableList<Scope> scopes, P param) {
		for (Scope scope : scopes) {
			traverseScope(scope, param);
		}
	}

	@Override
	public void traverseScope(Scope scope, P param) {
		traverseVarDecls(scope.getDeclarations(), param);
	}

	@Override
	public void traverseTransitions(ImmutableList<Transition> transitions, P param) {
		for (Transition t : transitions) {
			traverseTransition(t, param);
		}
	}

	@Override
	public void traverseTransition(Transition transition, P param) {
		traverseStatement(transition.getBody(), param);
	}

	@Override
	public void traverseConditions(ImmutableList<Condition> conditions, P param) {
		for (Condition c : conditions) {
			traverseCondition(c, param);
		}
	}

	@Override
	public void traverseCondition(Condition condition, P param) {
		condition.accept(this, param);
	}

	@Override
	public Void visitInputCondition(PortCondition c, P p) {
		traversePort(c.getPortName(), p);
		return null;
	}

	@Override
	public Void visitOutputCondition(PortCondition c, P p) {
		traversePort(c.getPortName(), p);
		return null;
	}

	@Override
	public Void visitPredicateCondition(PredicateCondition c, P p) {
		traverseExpression(c.getExpression(), p);
		return null;
	}

	@Override
	public void traverseController(ImmutableList<State> controller, P param) {
		for (State state : controller) {
			traverseControllerState(state, param);
		}
	}

	@Override
	public void traverseControllerState(State state, P param) {
		for (Instruction instr : state.getInstructions()) {
			traverseInstruction(instr, param);
		}
	}

	@Override
	public void traverseInstruction(Instruction instr, P param) {
		instr.accept(this, param);
	}

	@Override
	public Void visitWait(IWait i, P p) {
		return null;
	}

	@Override
	public Void visitTest(ITest i, P p) {
		return null;
	}

	@Override
	public Void visitCall(ICall i, P p) {
		return null;
	}

	@Override
	public void traverseInputPorts(ImmutableList<PortDecl> inputPorts, P param) {
		for (PortDecl in : inputPorts) {
			traverseInputPort(in, param);
		}
	}

	@Override
	public void traverseInputPort(PortDecl inputPort, P param) {
		traverseTypeExpr(inputPort.getType(), param);
	}

	@Override
	public void traverseOutputPorts(ImmutableList<PortDecl> outputPorts, P param) {
		for (PortDecl out : outputPorts) {
			traverseOutputPort(out, param);
		}
	}

	@Override
	public void traverseOutputPort(PortDecl outputPort, P param) {
		traverseTypeExpr(outputPort.getType(), param);
	}

}
