package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;
import java.util.function.Consumer;

public class InputVarDecl extends VarDecl {

    private static long count = 0;

    public InputVarDecl() {
        this(String.format("$input%d", count++));
    }

    public InputVarDecl(String name) {
        this(null, name);
    }

    private InputVarDecl(VarDecl original, String name) {
        this(original, name, null);
    }
    private InputVarDecl(VarDecl original, String name, TypeExpr type) {
        super(original, ImmutableList.empty(), type, name, null, true, false);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        if (getType() != null) {
            action.accept(getType());
        }
    }

    private InputVarDecl copy(String name, TypeExpr type) {
        if (Objects.equals(getName(), name) && type == getType()) {
            return this;
        } else {
            return new InputVarDecl(this, name, type);
        }
    }
    @Override
    public InputVarDecl withName(String name) {
        return copy(name, getType());
    }

    @Override
    public InputVarDecl withType(TypeExpr type) {
        return copy(getName(), type);
    }

    @Override
    public InputVarDecl transformChildren(Transformation transformation) {
        if (getType() == null)
            return this;
        else {
            return copy(
                    getName(), transformation.applyChecked(TypeExpr.class, getType())
            );
        }
    }

}
