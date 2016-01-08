package se.lth.cs.tycho.ir.entity.am;

import se.lth.cs.tycho.ir.entity.am.ctrl.Controller;
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


public class ActorMachine extends Entity {

	public Controller controller() {
		return controller;
	}

	public ImmutableList<Scope> getScopes() {
		return scopes;
	}

	public ImmutableList<Transition> getTransitions() {
		return transitions;
	}

	public ImmutableList<Condition> getConditions() {
		return conditions;
	}

	public Condition getCondition(int i) {
		return conditions.get(i);
	}


	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitActorMachine(this, param);
	}

	public ActorMachine(List<PortDecl> inputPorts, List<PortDecl> outputPorts,
						List<TypeDecl> typeParameters, List<VarDecl> valueParameters, List<Scope> scopes, Controller controller,
						List<Transition> transitions, List<Condition> conditions) {
		this(null, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
	}

	private ActorMachine(ActorMachine original, List<PortDecl> inputPorts,
						 List<PortDecl> outputPorts, List<TypeDecl> typeParameters, List<VarDecl> valueParameters, List<Scope> scopes,
						 Controller controller, List<Transition> transitions,
						 List<Condition> conditions) {
		super(original, inputPorts, outputPorts, typeParameters, valueParameters);
		this.controller = controller;
		this.scopes = ImmutableList.from(scopes);
		this.transitions = ImmutableList.from(transitions);
		this.conditions = ImmutableList.from(conditions);
	}

	public ActorMachine copy(List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<TypeDecl> typeParameters,
							 List<VarDecl> valueParameters,
							 List<Scope> scopes, Controller newController,
							 List<Transition> transitions, List<Condition> conditions) {
		if (Lists.elementIdentityEquals(getInputPorts(), inputPorts) && Lists.elementIdentityEquals(getOutputPorts(), outputPorts)
				&& Lists.elementIdentityEquals(this.typeParameters, typeParameters) && Lists.elementIdentityEquals(this.valueParameters, valueParameters)
				&& Lists.elementIdentityEquals(this.scopes, scopes) && Objects.equals(this.controller, newController)
				&& Lists.elementIdentityEquals(this.transitions, transitions) && Lists.elementIdentityEquals(this.conditions, conditions)) {
			return this;
		}
		return new ActorMachine(this, inputPorts, outputPorts, typeParameters, valueParameters, scopes, newController, transitions, conditions);
	}

	private final ImmutableList<Scope> scopes;
	private final ImmutableList<Transition> transitions;
	private final ImmutableList<Condition> conditions;
	private final Controller controller;


	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		super.forEachChild(action);
		scopes.forEach(action);
		transitions.forEach(action);
		conditions.forEach(action);
	}

	public ActorMachine withController(Controller ctrl) {
		if (this.controller == ctrl) {
			return this;
		} else {
			return new ActorMachine(this, getInputPorts(), getOutputPorts(), getTypeParameters(), getValueParameters(), scopes, ctrl, transitions, conditions);
		}
	}
}
