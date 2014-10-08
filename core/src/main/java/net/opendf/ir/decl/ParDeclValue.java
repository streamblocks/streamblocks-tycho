package net.opendf.ir.decl;

import java.util.Objects;

import net.opendf.ir.TypeExpr;

public class ParDeclValue extends VarDecl implements ParDecl {

	public ParDeclValue(String name, TypeExpr type) {
		this(null, name, type);
	}

	private ParDeclValue(ParDeclValue original, String name, TypeExpr type) {
		super(original, type, name);
	}
	
	public ParDeclValue copy(String name, TypeExpr type) {
		if (Objects.equals(getName(), name) && Objects.equals(getType(), type)) {
			return this;
		}
		return new ParDeclValue(this, name, type);
	}

	@Override
	public <R, P> R accept(ParDeclVisitor<R, P> visitor, P param) {
		return visitor.visitParDeclValue(this, param);
	}


}
