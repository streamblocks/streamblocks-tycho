package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class ExprProcReturn extends Expression {
    public ExprProcReturn(List<ParameterVarDecl> valueParams, List<Statement> body, TypeExpr returnTypeExpr) {
        this(null, valueParams, body, returnTypeExpr);
    }

    public ExprProcReturn(List<ParameterVarDecl> valueParams) {
        this(null, valueParams, null, null);
    }

    private ExprProcReturn(ExprProcReturn original, List<ParameterVarDecl> valueParams, List<Statement> body, TypeExpr returnTypeExpr) {
        super(original);
        this.valueParameters = ImmutableList.from(valueParams);
        this.body = ImmutableList.from(body);
        this.returnTypeExpr = returnTypeExpr;
    }

    private ExprProcReturn copy(List<ParameterVarDecl> valueParams, List<Statement> body, TypeExpr returnTypeExpr) {
        if (Lists.sameElements(valueParameters, valueParams)
                && Lists.sameElements(this.body, body)) {
            return this;
        }
        return new ExprProcReturn(this, valueParams, body, returnTypeExpr);
    }

    public ImmutableList<ParameterVarDecl> getValueParameters() {
        return valueParameters;
    }

    public ExprProcReturn withValueParameters(List<ParameterVarDecl> valueParameters) {
        return copy(valueParameters, body, returnTypeExpr);
    }

    public ImmutableList<Statement> getBody() {
        return body;
    }

    public ExprProcReturn withBody(List<Statement> body) {
        return copy(valueParameters, body, returnTypeExpr);
    }

    public TypeExpr getReturnType() {
        return returnTypeExpr;
    }

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
    public ExprProcReturn transformChildren(Transformation transformation) {
        return copy(
                transformation.mapChecked(ParameterVarDecl.class, valueParameters),
                transformation.mapChecked(Statement.class, body),
                returnTypeExpr == null ? null : transformation.applyChecked(TypeExpr.class, returnTypeExpr)
        );
    }
}
