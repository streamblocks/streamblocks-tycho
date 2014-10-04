package net.opendf.ir.common.decl;

import java.util.Objects;

import net.opendf.ir.IRNode;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.common.expr.Expression;

public class LocalVarDecl extends VarDecl implements LocalDecl {

	public Expression getInitialValue() {
		return initialValue;
	}

	public boolean isAssignable() {
		return isAssignable;
	}

	/**
	 * Create an assignable variable declaration with no initial value.
	 * @param type
	 * @param name
	 * @param namespace
	 */
	public LocalVarDecl(TypeExpr type, String name) {
		this(type, name, null, true);
	}

	public LocalVarDecl(TypeExpr type, String name, Expression initialValue, boolean isAssignable) {
		this(null, type, name, initialValue, isAssignable);
	}

	public LocalVarDecl(IRNode original, TypeExpr type, String name, Expression initialValue,
			boolean isAssignable) {

		super(original, type, name);

		this.initialValue = initialValue;
		this.isAssignable = isAssignable;
	}

	public LocalVarDecl copy(TypeExpr type, String name, Expression initialValue,
			boolean isAssignable) {
		if (Objects.equals(getType(), type) && Objects.equals(getName(), name)
				&& Objects.equals(this.initialValue, initialValue)
				&& this.isAssignable == isAssignable) {
			return this;
		}
		return new LocalVarDecl(this, type, name, initialValue, isAssignable);
	}
	
	public LocalVarDecl copy(TypeExpr type, String name) {
		return copy(type, name, null, true);
	}

	private Expression initialValue;
	private boolean isAssignable;
	
	@Override
	public <R, P> R accept(LocalDeclVisitor<R, P> visitor, P param) {
		return visitor.visitLocalVarDecl(this, param);
	}
}
