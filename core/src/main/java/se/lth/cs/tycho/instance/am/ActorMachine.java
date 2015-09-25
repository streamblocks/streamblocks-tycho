package se.lth.cs.tycho.instance.am;

import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.InstanceVisitor;
import se.lth.cs.tycho.instance.am.ctrl.Controller;
import se.lth.cs.tycho.instance.am.ctrl.LegacyAdaptor;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;

/**
 * This class contains a description of an calActor machine. The central structure
 * inside an calActor machine is its controller.
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
 * Along with the controller, an calActor machine contains a list of scopes, that
 * in turn are lists of {@link VarDecl declarations}. Each of these scopes
 * represents a set of temporary variable declarations that are referred to by
 * the {@link PredicateCondition predicate conditions} and the
 * {@link Transition transition code}. These bindings are valid until a
 * {@link ICall instruction} clears them.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class ActorMachine extends Instance {

	public ImmutableList<State> getController() {
		return controller;
	}

	public Controller controller() {
		return adaptor;
	}

	public ImmutableList<Instruction> getInstructions(int n) {
		return controller.get(n).getInstructions();
	}

	public ImmutableList<Scope> getScopes() {
		return scopes;
	}

	public ImmutableList<VarDecl> getScope(int i) {
		return scopes.get(i).getDeclarations();
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

	@Override
	public <R, P> R accept(InstanceVisitor<R, P> visitor, P param) {
		return visitor.visitActorMachine(this, param);
	}

	public ActorMachine(ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Scope> scopes, ImmutableList<State> controller,
			ImmutableList<Transition> transitions, ImmutableList<Condition> conditions) {
		this(null, inputPorts, outputPorts, scopes, controller, transitions, conditions);
	}

	private ActorMachine(ActorMachine original, ImmutableList<PortDecl> inputPorts,
			ImmutableList<PortDecl> outputPorts, ImmutableList<Scope> scopes,
			ImmutableList<State> controller, ImmutableList<Transition> transitions,
			ImmutableList<Condition> conditions) {
		super(original, inputPorts, outputPorts);
		this.scopes = ImmutableList.from(scopes);
		this.controller = ImmutableList.from(controller);
		this.transitions = ImmutableList.from(transitions);
		this.conditions = ImmutableList.from(conditions);
		adaptor = new LegacyAdaptor(controller);
	}

	public ActorMachine copy(ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<Scope> scopes, ImmutableList<State> controller,
			ImmutableList<Transition> transitions, ImmutableList<Condition> conditions) {
		if (Lists.equals(getInputPorts(), inputPorts) && Lists.equals(getOutputPorts(), outputPorts)
				&& Lists.equals(this.scopes, scopes) && Lists.equals(this.controller, controller)
				&& Lists.equals(this.transitions, transitions) && Lists.equals(this.conditions, conditions)) {
			return this;
		}
		return new ActorMachine(this, inputPorts, outputPorts, scopes, controller, transitions, conditions);
	}

	private final ImmutableList<Scope> scopes;
	private final ImmutableList<State> controller;
	private final ImmutableList<Transition> transitions;
	private final ImmutableList<Condition> conditions;
	private final Controller adaptor;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		super.forEachChild(action);
		scopes.forEach(action);
		controller.forEach(action);
		transitions.forEach(action);
		conditions.forEach(action);
	}
}
