package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class ClosureVarDecl extends VarDecl {

	public ClosureVarDecl(TypeExpr type, String name, Expression value) {
		this(null, type, name, value);
	}

	private ClosureVarDecl(VarDecl original, TypeExpr type, String name, Expression value) {
		super(original, type, name, value, true, false);
	}

	private ClosureVarDecl copy(TypeExpr type, String name, Expression value) {
		if (this.getType() == type && Objects.equals(this.getName(), name) && this.getValue() == value) {
			return this;
		} else {
			return new ClosureVarDecl(this, type, name, value);
		}
	}

	@Override
	public ClosureVarDecl withName(String name) {
		return copy(getType(), name, getValue());
	}

	public ClosureVarDecl withValue(Expression value) {
		return copy(getType(), getName(), value);
	}

	public ClosureVarDecl withType(TypeExpr type) {
		return copy(type, getName(), getValue());
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getType());
		action.accept(getValue());
	}

	@Override
	public ClosureVarDecl transformChildren(Transformation transformation) {
		return copy(
				transformation.applyChecked(TypeExpr.class, getType()),
				getName(),
				transformation.applyChecked(Expression.class, getValue()));
	}
}
