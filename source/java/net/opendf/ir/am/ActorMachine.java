package net.opendf.ir.am;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * This class contains a description of an actor machine. The central structure
 * inside an actor machine is its controller.
 * 
 * The controller is a list that contains for each index (the <it>controller
 * state</it>) a (possibly empty) list of {@link Instruction instructions} that
 * may be executed in that state. In addition to the code required to execute
 * the instructions, they also contain one or more successor states that the
 * controller transitions to after execution.
 * 
 * The initial controller state is assumed to be 0, and the controller must at
 * least contain a single state.
 * 
 * The actual code that is executed is contained with the {@link ICall call} and
 * {@link ITest test} instructions.
 * 
 * Along with the controller, an actor machine contains a list of scopes, that
 * in turn are lists of {@link DeclVar declarations}. Each of these scopes
 * represents a set of temporary variable declarations that are referred to by
 * the {@link PredicateCondition predicate conditions} and the
 * {@link Transition transition code}. These bindings are valid until a
 * {@link ICall instruction} clears them.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class ActorMachine extends AbstractIRNode implements PortContainer {

	public ImmutableList<State> getController() {
		return controller;
	}

	public ImmutableList<Instruction> getInstructions(int n) {
		return controller.get(n).getInstructions();
	}

	public ImmutableList<Scope> getScopes() {
		return scopes;
	}

	public ImmutableList<DeclVar> getScope(int i) {
		return scopes.get(i).getDeclarations();
	}

	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}

	public ImmutableList<Transition> getTransitions() {
		return transitions;
	}

	public Transition getTransition(int i) {
		return transitions.get(i);
	}

	public ImmutableList<Condition> getConditions() {
		return conditions;
	}

	public Condition getCondition(int i) {
		return conditions.get(i);
	}

	public ActorMachine(ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Scope> scopes, ImmutableList<State> controller,
			ImmutableList<Transition> transitions, ImmutableList<Condition> conditions) {
		this(null, inputPorts, outputPorts, scopes, controller, transitions, conditions);
	}

	public ActorMachine(IRNode original, ImmutableList<PortDecl> inputPorts,
			ImmutableList<PortDecl> outputPorts, ImmutableList<Scope> scopes,
			ImmutableList<State> controller, ImmutableList<Transition> transitions,
			ImmutableList<Condition> conditions) {
		super(original);
		this.scopes = ImmutableList.copyOf(scopes);
		this.controller = ImmutableList.copyOf(controller);
		this.inputPorts = ImmutableList.copyOf(inputPorts);
		this.outputPorts = ImmutableList.copyOf(outputPorts);
		this.transitions = ImmutableList.copyOf(transitions);
		this.conditions = ImmutableList.copyOf(conditions);
	}

	public ActorMachine copy(ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Scope> scopes, ImmutableList<State> controller,
			ImmutableList<Transition> transitions, ImmutableList<Condition> conditions) {
		if (Lists.equals(this.inputPorts, inputPorts) && Lists.equals(this.outputPorts, outputPorts)
				&& Lists.equals(this.scopes, scopes) && Lists.equals(this.controller, controller)
				&& Lists.equals(this.transitions, transitions) && Lists.equals(this.conditions, conditions)) {
			return this;
		}
		return new ActorMachine(this, inputPorts, outputPorts, scopes, controller, transitions, conditions);
	}

	private ImmutableList<Scope> scopes;
	private ImmutableList<State> controller;
	private ImmutableList<PortDecl> inputPorts;
	private ImmutableList<PortDecl> outputPorts;
	private ImmutableList<Transition> transitions;
	private ImmutableList<Condition> conditions;
}
