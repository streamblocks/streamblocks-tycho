package se.lth.cs.tycho.ir.entity.am;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.ParameterTypeDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ctrl.Controller;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;


public class ActorMachine extends Entity {

    public Controller controller() {
        return controller;
    }

    public ImmutableList<Scope> getScopes() {
        return scopes;
    }

    public ActorMachine withScopes(List<Scope> scopes) {
        return copy(annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    public ImmutableList<Transition> getTransitions() {
        return transitions;
    }

    public ActorMachine withTransitions(List<Transition> transitions) {
        return copy(annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    public ImmutableList<Condition> getConditions() {
        return conditions;
    }

    public ActorMachine withConditions(List<Condition> conditions) {
        return copy(annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    public Condition getCondition(int i) {
        return conditions.get(i);
    }

    public ActorMachine withInputPorts(List<PortDecl> inputPorts) {
        return copy(annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    public ActorMachine withOutputPorts(List<PortDecl> outputPorts) {
        return copy(annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    @Override
    public Entity withTypeParameters(List<ParameterTypeDecl> typeParameters) {
        return copy(annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    @Override
    public Entity withValueParameters(List<ParameterVarDecl> valueParameters) {
        return copy(annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    public ActorMachine withAnnotations(List<Annotation> annotations) {
        return copy(annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    public ActorMachine(List<Annotation> annotations, List<PortDecl> inputPorts, List<PortDecl> outputPorts,
                        List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters, List<Scope> scopes, Controller controller,
                        List<Transition> transitions, List<Condition> conditions) {
        this(null, annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    private ActorMachine(ActorMachine original, List<Annotation> annotations, List<PortDecl> inputPorts,
                         List<PortDecl> outputPorts, List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters, List<Scope> scopes,
                         Controller controller, List<Transition> transitions,
                         List<Condition> conditions) {
        super(original, annotations, inputPorts, outputPorts, typeParameters, valueParameters);
        this.controller = controller;
        this.scopes = ImmutableList.from(scopes);
        this.transitions = ImmutableList.from(transitions);
        this.conditions = ImmutableList.from(conditions);
        this.annotations = ImmutableList.from(annotations);
    }

    public ActorMachine copy(List<Annotation> annotations, List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<ParameterTypeDecl> typeParameters,
                             List<ParameterVarDecl> valueParameters,
                             List<Scope> scopes, Controller controller,
                             List<Transition> transitions, List<Condition> conditions) {
        if (Lists.sameElements(this.annotations, annotations)
                && Lists.sameElements(this.inputPorts, inputPorts)
                && Lists.sameElements(this.outputPorts, outputPorts)
                && Lists.sameElements(this.typeParameters, typeParameters)
                && Lists.sameElements(this.valueParameters, valueParameters)
                && Lists.sameElements(this.scopes, scopes)
                && this.controller == controller
                && Lists.sameElements(this.transitions, transitions)
                && Lists.sameElements(this.conditions, conditions)) {
            return this;
        }
        return new ActorMachine(this, annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, controller, transitions, conditions);
    }

    private final ImmutableList<Annotation> annotations;
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

    @Override
    public ActorMachine transformChildren(Transformation transformation) {
        return copy(
                (ImmutableList) annotations.map(transformation),
                (ImmutableList) inputPorts.map(transformation),
                (ImmutableList) outputPorts.map(transformation),
                (ImmutableList) typeParameters.map(transformation),
                (ImmutableList) valueParameters.map(transformation),
                (ImmutableList) scopes.map(transformation),
                controller,
                (ImmutableList) transitions.map(transformation),
                (ImmutableList) conditions.map(transformation)
        );
    }

    @Override
    public ActorMachine clone() {
        return (ActorMachine) super.clone();
    }

    @Override
    public ActorMachine deepClone() {
        return (ActorMachine) super.deepClone();
    }

    public ActorMachine withController(Controller ctrl) {
        if (this.controller == ctrl) {
            return this;
        } else {
            return new ActorMachine(this, annotations, inputPorts, outputPorts, typeParameters, valueParameters, scopes, ctrl, transitions, conditions);
        }
    }
}
