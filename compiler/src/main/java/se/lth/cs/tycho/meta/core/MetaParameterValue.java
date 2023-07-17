package se.lth.cs.tycho.meta.core;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.type.Type;

import java.util.Objects;
import java.util.function.Consumer;

public class MetaParameterValue extends MetaParameter {

	private final Expression value;

	private final TypeExpr type;

	public MetaParameterValue(String name, Expression value, TypeExpr type) {
		this(null, name, value, type);
	}

	public MetaParameterValue(IRNode original, String name, Expression value, TypeExpr type) {
		super(original, name);
		this.value = value;
		this.type = type;
	}

	public MetaParameterValue copy(String name, Expression value, TypeExpr type) {
		if (Objects.equals(getName(), name) && Objects.equals(getValue(), value) && Objects.equals(getType(), type)) {
			return this;
		} else {
			return new MetaParameterValue(this, name, value, type);
		}
	}

	public Expression getValue() {
		return value;
	}

	public TypeExpr getType() {
		return type;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (getValue() != null) action.accept(getValue());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(getName(),
				getValue() == null ? null : transformation.applyChecked(Expression.class, getValue()),
				getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()));
	}
}