/* 
BEGINCOPYRIGHT JWJ
ENDCOPYRIGHT
 */

package net.opendf.ir.common.decl;

import java.util.Objects;

import net.opendf.ir.IRNode;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.common.expr.Expression;

public class DeclVar extends Decl {

	@Override
	public DeclKind getKind() {
		return DeclKind.value;
	}

	@Override
	public <R, P> R accept(DeclVisitor<R, P> v, P p) {
		return v.visitDeclVar(this, p);
	}

	public Expression getInitialValue() {
		return initialValue;
	}

	public TypeExpr getType() {
		return typeExpr;
	}

	public boolean isAssignable() {
		return isAssignable;
	}

	//
	// Ctor
	//

	/**
	 * Create an assignable variable declaration with no initial value.
	 * @param type
	 * @param name
	 * @param namespace
	 */
	public DeclVar(TypeExpr type, String name) {
		this(type, name, null, true);
	}

	public DeclVar(TypeExpr type, String name, Expression initialValue, boolean isAssignable) {
		this(null, type, name, initialValue, isAssignable);
	}

	public DeclVar(IRNode original, TypeExpr type, String name, Expression initialValue,
			boolean isAssignable) {

		super(original, name);

		this.typeExpr = type;
		this.initialValue = initialValue;
		this.isAssignable = isAssignable;
	}

	public DeclVar copy(TypeExpr type, String name, Expression initialValue,
			boolean isAssignable) {
		if (Objects.equals(typeExpr, type) && Objects.equals(getName(), name)
				&& Objects.equals(this.initialValue, initialValue)
				&& this.isAssignable == isAssignable) {
			return this;
		}
		return new DeclVar(this, type, name, initialValue, isAssignable);
	}
	
	public DeclVar copy(TypeExpr type, String name) {
		return copy(type, name, null, true);
	}

	private Expression initialValue;
	private TypeExpr typeExpr;
	private boolean isAssignable;
}
