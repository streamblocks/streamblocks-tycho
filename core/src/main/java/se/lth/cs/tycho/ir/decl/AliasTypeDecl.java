package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;
import java.util.function.Consumer;

public class AliasTypeDecl extends GlobalTypeDecl {

	private final TypeExpr type;

	public AliasTypeDecl(String name, Availability availability, TypeExpr type) {
		this(null, name, availability, type);
	}

	public AliasTypeDecl(TypeDecl original, String name, Availability availability, TypeExpr type) {
		super(original, name, availability);
		this.type = type;
	}

	public AliasTypeDecl copy(String name, Availability availability, TypeExpr type) {
		if (Objects.equals(getName(), name) && Objects.equals(getAvailability(), availability) && Objects.equals(getType(), type)) {
			return this;
		} else {
			return new AliasTypeDecl(this, name, availability, type);
		}
	}

	public TypeExpr getType() {
		return type;
	}

	@Override
	public GlobalDecl withAvailability(Availability availability) {
		return copy(getName(), availability, getType());
	}

	@Override
	public Decl withName(String name) {
		return copy(name, getAvailability(), getType());
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getType());
	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return copy(getName(), getAvailability(), transformation.applyChecked(TypeExpr.class, getType()));
	}
}
