package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ParameterVarDecl extends VarDecl {
    private final Expression defaultValue;

    public ParameterVarDecl(List<Annotation> annotations, TypeExpr type, String name, Expression defaultValue) {
        this(null, annotations, type, name, defaultValue);
    }

    private ParameterVarDecl(VarDecl original, List<Annotation> annotations, TypeExpr type, String name, Expression defaultValue) {
        super(original, annotations, type, name, null, false, false);
        this.defaultValue = defaultValue;
    }

    public ParameterVarDecl copy(List<Annotation> annotations, TypeExpr type, String name, Expression defaultValue) {
        if (Objects.equals(getAnnotations(), annotations) && getType() == type && Objects.equals(getName(), name) && getDefaultValue() == defaultValue) {
            return this;
        } else {
            return new ParameterVarDecl(this, annotations, type, name, defaultValue);
        }
    }

    public Expression getDefaultValue() {
        return defaultValue;
    }

    public ParameterVarDecl withDefaultValue(Expression defaultValue) {
        return copy(getAnnotations(), getType(), getName(), defaultValue);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        if (getType() != null) action.accept(getType());
        if (getValue() != null) action.accept(getValue());
    }

    @Override
    public ParameterVarDecl transformChildren(Transformation transformation) {
        return copy(
                getAnnotations(),
                getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()),
                getName(),
                getDefaultValue() == null ? null : transformation.applyChecked(Expression.class, defaultValue));
    }

    public ParameterVarDecl withType(TypeExpr type) {
        return copy(getAnnotations(), type, getName(), defaultValue);
    }

    @Override
    public ParameterVarDecl withName(String name) {
        return copy(getAnnotations(), getType(), name, defaultValue);
    }
}
