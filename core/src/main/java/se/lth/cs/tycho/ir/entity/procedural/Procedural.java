package se.lth.cs.tycho.ir.entity.procedural;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.ParameterTypeDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class Procedural extends Entity {
    public Procedural(Procedural original, List<Annotation> annotations, List<PortDecl> inputPorts, List<PortDecl> outputPorts,
                      List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters, List<VarDecl> functions) {
        super(original, annotations, inputPorts, outputPorts, typeParameters, valueParameters);
        this.functions = ImmutableList.from(functions);
    }

    public Procedural(List<Annotation> annotations, List<PortDecl> inputPorts, List<PortDecl> outputPorts,
                      List<ParameterTypeDecl> typeParameters, List<ParameterVarDecl> valueParameters, List<VarDecl> functions) {
        this(null, annotations, inputPorts, outputPorts, typeParameters, valueParameters, functions);
    }


    public Procedural copy(List<Annotation> annotations, List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<ParameterTypeDecl> typeParameters,
                           List<ParameterVarDecl> valueParameters, List<VarDecl> functions) {
        if (Lists.sameElements(this.annotations, annotations)
                && Lists.sameElements(this.inputPorts, inputPorts)
                && Lists.sameElements(this.outputPorts, outputPorts)
                && Lists.sameElements(this.typeParameters, typeParameters)
                && Lists.sameElements(this.valueParameters, valueParameters)
                && Lists.sameElements(this.functions, functions)) {
            return this;
        }
        return new Procedural(this, annotations, inputPorts, outputPorts, typeParameters, valueParameters, functions);
    }


    @Override
    public Entity withTypeParameters(List<ParameterTypeDecl> typeParameters) {
        return null;
    }

    @Override
    public Entity withValueParameters(List<ParameterVarDecl> valueParameters) {
        return null;
    }

    private final ImmutableList<VarDecl> functions;

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        super.forEachChild(action);
        functions.forEach(action);
    }

    @Override
    public Procedural transformChildren(Transformation transformation) {
        return copy(
                (ImmutableList) annotations.map(transformation),
                (ImmutableList) inputPorts.map(transformation),
                (ImmutableList) outputPorts.map(transformation),
                (ImmutableList) typeParameters.map(transformation),
                (ImmutableList) valueParameters.map(transformation),
                (ImmutableList) functions.map(transformation)
        );
    }

    @Override
    public Procedural clone() {
        return (Procedural) super.clone();
    }

    @Override
    public Procedural deepClone() {
        return (Procedural) super.deepClone();
    }

}
