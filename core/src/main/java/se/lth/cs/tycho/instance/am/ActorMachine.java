package se.lth.cs.tycho.instance.am;

import se.lth.cs.tycho.instance.am.ctrl.Controller;
import se.lth.cs.tycho.instance.am.ctrl.LegacyAdaptor;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * This class contains a description of an calActor machine. The central structure
 * inside an calActor machine is its controller.
 * <p/>
 * The controller is a list that contains for each index (the <it>controller
 * state</it>) a (possibly empty) list of {@link Instruction instructions} that
 * may be executed in that state. In addition to the code required to execute
 * the instructions, they also contain one or more successor states that the
 * controller transitions to after execution.
 * <p/>
 * The initial controller state is assumed to be 0, and the controller must at
 * least contain a single state.
 * <p/>
 * The actual code that is executed is contained with the {@link ICall call} and
 * {@link ITest test} instructions.
 * <p/>
 * Along with the controller, an calActor machine contains a list of scopes, that
 * in turn are lists of {@link VarDecl declarations}. Each of these scopes
 * represents a set of temporary variable declarations that are referred to by
 * the {@link PredicateCondition predicate conditions} and the
 * {@link Transition transition code}. These bindings are valid until a
 * {@link ICall instruction} clears them.
 *
 * @author Jorn W. Janneck <jwj@acm.org>
 */

public class ActorMachine extends Entity {

	public ImmutableList<State> getController() {
		return controller;
	}

	public Controller controller() {
		return newController;
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
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		throw new Error();
	}

	public ActorMachine(List<PortDecl> inputPorts, List<PortDecl> outputPorts,
						List<TypeDecl> typeParameters, List<VarDecl> valueParameters, List<Scope> scopes, List<State> controller,
						List<Transition> transitions, List<Condition> conditions) {
		this(null, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, null, transitions, conditions);
	}

	public ActorMachine(List<PortDecl> inputPorts, List<PortDecl> outputPorts,
						List<TypeDecl> typeParameters, List<VarDecl> valueParameters, List<Scope> scopes, Controller controller,
						List<Transition> transitions, List<Condition> conditions) {
		this(null, inputPorts, outputPorts, typeParameters, valueParameters, scopes, null, controller, transitions, conditions);
	}

	private ActorMachine(ActorMachine original, List<PortDecl> inputPorts,
						 List<PortDecl> outputPorts, List<TypeDecl> typeParameters, List<VarDecl> valueParameters, List<Scope> scopes,
						 List<State> controller, Controller newController, List<Transition> transitions,
						 List<Condition> conditions) {
		super(original, inputPorts, outputPorts, typeParameters, valueParameters);
		this.newController = newController;
		this.scopes = ImmutableList.from(scopes);
		this.controller = ImmutableList.from(controller);
		this.transitions = ImmutableList.from(transitions);
		this.conditions = ImmutableList.from(conditions);
	}

	public ActorMachine copy(List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<TypeDecl> typeParameters,
							 List<VarDecl> valueParameters,
							 List<Scope> scopes, List<State> controller,
							 List<Transition> transitions, List<Condition> conditions) {
		if (Lists.elementIdentityEquals(getInputPorts(), inputPorts) && Lists.elementIdentityEquals(getOutputPorts(), outputPorts)
				&& Lists.elementIdentityEquals(this.typeParameters, typeParameters) && Lists.elementIdentityEquals(this.valueParameters, valueParameters)
				&& Lists.elementIdentityEquals(this.scopes, scopes) && Lists.elementIdentityEquals(this.controller, controller)
				&& Lists.elementIdentityEquals(this.transitions, transitions) && Lists.elementIdentityEquals(this.conditions, conditions)) {
			return this;
		}
		return new ActorMachine(this, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, null, transitions, conditions);
	}

	public ActorMachine copy(List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<TypeDecl> typeParameters,
							 List<VarDecl> valueParameters,
							 List<Scope> scopes, Controller newController,
							 List<Transition> transitions, List<Condition> conditions) {
		if (Lists.elementIdentityEquals(getInputPorts(), inputPorts) && Lists.elementIdentityEquals(getOutputPorts(), outputPorts)
				&& Lists.elementIdentityEquals(this.typeParameters, typeParameters) && Lists.elementIdentityEquals(this.valueParameters, valueParameters)
				&& Lists.elementIdentityEquals(this.scopes, scopes) && Objects.equals(this.newController, newController)
				&& Lists.elementIdentityEquals(this.transitions, transitions) && Lists.elementIdentityEquals(this.conditions, conditions)) {
			return this;
		}
		return new ActorMachine(this, inputPorts, outputPorts, typeParameters, valueParameters, scopes, null, newController, transitions, conditions);
	}

	private final ImmutableList<Scope> scopes;
	private final ImmutableList<State> controller;
	private final ImmutableList<Transition> transitions;
	private final ImmutableList<Condition> conditions;
	private final Controller newController;


	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		super.forEachChild(action);
		scopes.forEach(action);
		controller.forEach(action);
		transitions.forEach(action);
		conditions.forEach(action);
	}

	public ActorMachine withController(Controller ctrl) {
		if (this.newController == ctrl) {
			return this;
		} else {
			return new ActorMachine(this, getInputPorts(), getOutputPorts(), getTypeParameters(), getValueParameters(), scopes, null, ctrl, transitions, conditions);
		}
	}
}
