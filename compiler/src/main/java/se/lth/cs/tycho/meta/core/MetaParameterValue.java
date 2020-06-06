package se.lth.cs.tycho.meta.core;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class MetaParameterValue extends MetaParameter {

	private final Expression value;

	public MetaParameterValue(String name, Expression value) {
		this(null, name, value);
	}

	public MetaParameterValue(IRNode original, String name, Expression value) {
		super(original, name);
		this.value = value;
	}

	public MetaParameterValue copy(String name, Expression value) {
		if (Objects.equals(getName(), name) && Objects.equals(getValue(), value)) {
			return this;
		} else {
			return new MetaParameterValue(this, name, value);
		}
	}

	public Expression getValue() {
		return value;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (getValue() != null) action.accept(getValue());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(getName(), getValue() == null ? null : transformation.applyChecked(Expression.class, getValue()));
	}
}
