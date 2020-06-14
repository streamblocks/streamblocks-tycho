package se.lth.cs.tycho.meta.core;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class MetaArgumentValue extends MetaArgument {

	private final Expression value;

	public MetaArgumentValue(String name, Expression value) {
		this(null, name, value);
	}

	public MetaArgumentValue(IRNode original, String name, Expression value) {
		super(original, name);
		this.value = value;
	}

	public MetaArgumentValue copy(String name, Expression value) {
		if (Objects.equals(getName(), name) && Objects.equals(getValue(), value)) {
			return this;
		} else {
			return new MetaArgumentValue(this, name, value);
		}
	}

	public Expression getValue() {
		return value;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getValue());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(getName(), transformation.applyChecked(Expression.class, getValue()));
	}
}
