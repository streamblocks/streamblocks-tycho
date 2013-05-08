/* 
BEGINCOPYRIGHT JWJ
ENDCOPYRIGHT
 */

package net.opendf.ir.common;

import java.util.Objects;

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

	public DeclVar(TypeExpr type, String name, NamespaceDecl namespace) {
		this(type, name, namespace, null, true);
	}

	public DeclVar(TypeExpr type, String name, NamespaceDecl namespace, Expression initialValue, boolean isAssignable) {
		this(null, type, name, namespace, initialValue, isAssignable);
	}

	private DeclVar(DeclVar original, TypeExpr type, String name, NamespaceDecl namespace, Expression initialValue,
			boolean isAssignable) {

		super(original, name, namespace);

		this.typeExpr = type;
		this.initialValue = initialValue;
		this.isAssignable = isAssignable;
	}

	public DeclVar copy(TypeExpr type, String name, NamespaceDecl namespace, Expression initialValue,
			boolean isAssignable) {
		if (Objects.equals(typeExpr, type) && Objects.equals(getName(), name)
				&& Objects.equals(getNamespaceDecl(), namespace) && Objects.equals(this.initialValue, initialValue)
				&& this.isAssignable == isAssignable) {
			return this;
		}
		return new DeclVar(this, type, name, namespace, initialValue, isAssignable);
	}
	
	public DeclVar copy(TypeExpr type, String name, NamespaceDecl namespace) {
		return copy(type, name, namespace, null, true);
	}

	private Expression initialValue;
	private TypeExpr typeExpr;
	private boolean isAssignable;
}
