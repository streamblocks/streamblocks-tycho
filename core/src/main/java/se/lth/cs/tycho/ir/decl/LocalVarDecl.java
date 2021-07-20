package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class LocalVarDecl extends VarDecl {
    public LocalVarDecl(List<Annotation> annotations, TypeExpr type, String name, Expression value, boolean constant) {
        this(null, annotations, type, name, value, constant, false);
    }

    private LocalVarDecl(VarDecl original, List<Annotation> annotations, TypeExpr type, String name, Expression value, boolean constant, boolean external) {
        super(original, annotations, type, name, value, constant, external);
    }

    private LocalVarDecl copy(List<Annotation> annotations, TypeExpr type, String name, Expression value, boolean constant, boolean external) {
        if (Objects.equals(getAnnotations(), annotations) && getType() == type && Objects.equals(getName(), name) && isConstant() == constant && getValue() == value && isExternal() == external) {
            return this;
        } else {
            return new LocalVarDecl(this, annotations, type, name, value, constant, external);
        }
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        if (getType() != null) action.accept(getType());
        if (getValue() != null) action.accept(getValue());
    }

    @Override
    public LocalVarDecl transformChildren(Transformation transformation) {
        return copy(
                getAnnotations(),
                getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()),
                getName(),
                getValue() == null ? null : transformation.applyChecked(Expression.class, getValue()),
                isConstant(),
                isExternal());
    }

    @Override
    public LocalVarDecl withType(TypeExpr type) {
        return copy(getAnnotations(), type, getName(), getValue(), isConstant(), isExternal());
    }

    public LocalVarDecl withValue(Expression value) {
        return copy(getAnnotations(), getType(), getName(), value, isConstant(), isExternal());
    }

    public LocalVarDecl asExternal(boolean external) {
        return copy(getAnnotations(), getType(), getName(), getValue(), isConstant(), external);
    }

    @Override
    public LocalVarDecl withName(String name) {
        return copy(getAnnotations(), getType(), name, getValue(), isConstant(), isExternal());
    }

}
