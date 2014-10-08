package net.opendf.ir.decl;

import java.util.Objects;

import net.opendf.ir.IRNode;
import net.opendf.ir.TypeExpr;
import net.opendf.ir.expr.Expression;

public class GlobalVarDecl extends VarDecl implements GlobalDecl {
	private final Expression value;
	private final Visibility visibility;

	public Expression getValue() {
		return value;
	}

	@Override
	public Visibility getVisibility() {
		return visibility;
	}

	public GlobalVarDecl(TypeExpr type, String name, Expression value, Visibility visibility) {
		this(null, type, name, value, visibility);
	}

	public GlobalVarDecl(IRNode original, TypeExpr type, String name, Expression value, Visibility visibility) {

		super(original, type, name);

		this.value = value;
		this.visibility = visibility;
	}

	public GlobalVarDecl copy(TypeExpr type, String name, Expression value, Visibility visibility) {
		if (Objects.equals(getType(), type) && Objects.equals(getName(), name)
				&& Objects.equals(this.value, value) && getVisibility() == visibility) {
			return this;
		}
		return new GlobalVarDecl(this, type, name, value, visibility);
	}

	@Override
	public <R, P> R accept(GlobalDeclVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalVarDecl(this, param);
	}
}
