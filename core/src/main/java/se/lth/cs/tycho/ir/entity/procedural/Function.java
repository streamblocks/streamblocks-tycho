package se.lth.cs.tycho.ir.entity.procedural;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class Function extends AbstractIRNode {


    public Function(String name, List<ParameterVarDecl> valueParams, List<Statement> body, TypeExpr returnTypeExpr) {
        this(null, name, valueParams, body, returnTypeExpr);
    }

    public Function(Function original, String name, List<ParameterVarDecl> valueParams, List<Statement> body, TypeExpr returnTypeExpr) {
        super(original);
        this.name = name;
        this.valueParameters = ImmutableList.from(valueParams);
        this.body = ImmutableList.from(body);
        this.returnTypeExpr = returnTypeExpr;
    }

    public Function copy(String name, List<ParameterVarDecl> valueParams, List<Statement> body, TypeExpr returnTypeExpr) {
        if (this.name.equals(name) && Lists.sameElements(valueParameters, valueParams)
                && Lists.sameElements(this.body, body) && this.returnTypeExpr == returnTypeExpr) {
            return this;
        }
        return new Function(this, name, valueParams, body, returnTypeExpr);
    }

    public String getName() {
        return name;
    }

    public ImmutableList<ParameterVarDecl> getValueParameters() {
        return valueParameters;
    }

    public ImmutableList<Statement> getBody() {
        return body;
    }

    public TypeExpr getReturnType() {
        return returnTypeExpr;
    }

    private final String name;
    private final ImmutableList<ParameterVarDecl> valueParameters;
    private final ImmutableList<Statement> body;
    private final TypeExpr returnTypeExpr;


    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        valueParameters.forEach(action);
        body.forEach(action);
        if (returnTypeExpr != null) action.accept(returnTypeExpr);
    }

    @Override
    public IRNode transformChildren(Transformation transformation) {
        return copy(
                getName(),
                transformation.mapChecked(ParameterVarDecl.class, valueParameters),
                transformation.mapChecked(Statement.class, body),
                returnTypeExpr == null ? null : transformation.applyChecked(TypeExpr.class, returnTypeExpr)
        );
    }

    @Override
    public Function deepClone() {
        return (Function) super.deepClone();
    }
}
