package se.lth.cs.tycho.meta.core;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class MetaArgumentValue extends MetaArgument {

    private final Expression value;

    private final boolean external;

    public MetaArgumentValue(String name, Expression value) {
        this(null, name, value, false);
    }

    public MetaArgumentValue(String name, Expression value, boolean external) {
        this(null, name, value, external);
    }

    public MetaArgumentValue(IRNode original, String name, Expression value, boolean external) {
        super(original, name);
        this.value = value;
        this.external = external;
    }

    public MetaArgumentValue copy(String name, Expression value, boolean external) {
        if (Objects.equals(getName(), name) && Objects.equals(getValue(), value)) {
            return this;
        } else {
            return new MetaArgumentValue(this, name, value, external);
        }
    }

    public Expression getValue() {
        return value;
    }

    public boolean isExternal() {
        return external;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(getValue());
    }

    @Override
    public IRNode transformChildren(Transformation transformation) {
        return copy(getName(), transformation.applyChecked(Expression.class, getValue()), isExternal());
    }
}
