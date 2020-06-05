package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GlobalVarDecl extends VarDecl implements GlobalDecl {
    private final Availability availability;

    public GlobalVarDecl(List<Annotation> annotations, Availability availability, TypeExpr type, String name, Expression value) {
        this(null, annotations, availability, type, name, value, false);
    }

    private GlobalVarDecl(VarDecl original, List<Annotation> annotations, Availability availability, TypeExpr type, String name, Expression value, boolean external) {
        super(original, annotations, type, name, value, true, external);
        this.availability = availability;
    }

    private GlobalVarDecl copy(List<Annotation> annotations, Availability availability, TypeExpr type, String name, Expression value, boolean external) {
        if (Objects.equals(getAnnotations(), annotations) && this.availability == availability && getType() == type && Objects.equals(getName(), name) && getValue() == value && isExternal() == external) {
            return this;
        } else {
            return new GlobalVarDecl(this, annotations, availability, type, name, value, external);
        }
    }

    @Override
    public Availability getAvailability() {
        return availability;
    }

    @Override
    public GlobalVarDecl withAvailability(Availability availability) {
        return copy(getAnnotations(), availability, getType(), getName(), getValue(), isExternal());
    }

    public GlobalVarDecl withType(TypeExpr type) {
        return copy(getAnnotations(), availability, type, getName(), getValue(), isExternal());
    }

    public GlobalVarDecl withValue(Expression value) {
        return copy(getAnnotations(), availability, getType(), getName(), value, isExternal());
    }

    @Override
    public GlobalVarDecl withName(String name) {
        return copy(getAnnotations(), availability, getType(), name, getValue(), isExternal());
    }

    public GlobalVarDecl asExternal(boolean external) {
        return copy(getAnnotations(), availability, getType(), getName(), getValue(), external);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        if (getType() != null) action.accept(getType());
        if (getValue() != null) action.accept(getValue());
    }

    @Override
    public GlobalVarDecl transformChildren(Transformation transformation) {
        return copy(
                getAnnotations(),
                availability,
                getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()),
                getName(),
                getValue() == null ? null : transformation.applyChecked(Expression.class, getValue()),
                isExternal());
    }

}
