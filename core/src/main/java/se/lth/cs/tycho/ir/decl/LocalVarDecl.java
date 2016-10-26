package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class LocalVarDecl extends VarDecl {
	public LocalVarDecl(TypeExpr type, String name, Expression value, boolean constant) {
		this(null, type, name, value, constant, false);
	}
	private LocalVarDecl(LocalVarDecl original, TypeExpr type, String name, Expression value, boolean constant, boolean external) {
		super(original, type, name, value, constant, external);
	}

	private LocalVarDecl copy(TypeExpr type, String name, Expression value, boolean constant, boolean external) {
		if (getType() == type && Objects.equals(getName(), name) && isConstant() == constant && getValue() == value && isExternal() == external) {
			return this;
		} else {
			return new LocalVarDecl(this, type, name, value, constant, external);
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
				getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()),
				getName(),
				getValue() == null ? null : transformation.applyChecked(Expression.class, getValue()), isConstant(),
				isExternal());
	}

	public LocalVarDecl withType(TypeExpr type) {
		return copy(type, getName(), getValue(), isConstant(), isExternal());
	}

	public LocalVarDecl withValue(Expression value) {
		return copy(getType(), getName(), value, isConstant(), isExternal());
	}

	public LocalVarDecl asExternal(boolean external) {
		return copy(getType(), getName(), getValue(), isConstant(), external);
	}

	@Override
	public LocalVarDecl withName(String name) {
		return copy(getType(), name, getValue(), isConstant(), isExternal());
	}

}
