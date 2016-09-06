package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;

public class ValueParameter extends AbstractIRNode implements Parameter<Expression, ValueParameter> {
	private final String name;
	private final Expression value;

	private ValueParameter(IRNode original, String name, Expression value) {
		super(original);
		this.name = name;
		this.value = value;
	}

	public ValueParameter(String name, Expression value) {
		this(null, name, value);
	}

	@Override
	public ValueParameter copy(String name, Expression value) {
		if (Objects.equals(this.name, name) && this.value == value) {
			return this;
		} else {
			return new ValueParameter(this, name, value);
		}
	}

	@Override
	public ValueParameter clone() {
		return (ValueParameter) super.clone();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Expression getValue() {
		return value;
	}

}
