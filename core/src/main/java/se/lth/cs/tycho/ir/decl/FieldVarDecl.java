package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;
import java.util.function.Consumer;

public class FieldVarDecl extends VarDecl {

    public FieldVarDecl(TypeExpr type, String name) {
        this(null, type, name);
    }

    private FieldVarDecl(VarDecl original, TypeExpr type, String name) {
        super(original, type, name, null, false, false);
    }

    public FieldVarDecl withType(TypeExpr type) {
        return copy(type, getName());
    }

    @Override
    public FieldVarDecl withName(String name) {
        return copy(getType(), name);
    }

    private FieldVarDecl copy(TypeExpr type, String name) {
        if (getType() == type && Objects.equals(getName(), name)) {
            return this;
        } else {
            return new FieldVarDecl(this, type, name);
        }
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        if (getType() != null) action.accept(getType());
    }

    @Override
    public VarDecl transformChildren(Transformation transformation) {
         return copy(
                getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()),
                getName());
    }
}
