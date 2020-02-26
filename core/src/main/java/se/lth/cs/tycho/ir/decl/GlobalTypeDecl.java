package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GlobalTypeDecl extends TypeDecl implements GlobalDecl {

	private Availability availability;
	private AlgebraicTypeDecl declaration;

	public GlobalTypeDecl(String name, Availability availability, AlgebraicTypeDecl declaration) {
		this(null, name, availability, declaration);
	}

	private GlobalTypeDecl(TypeDecl original, String name, Availability availability, AlgebraicTypeDecl declaration) {
		super(original, name);
		this.availability = availability;
		this.declaration = declaration;
	}

	public AlgebraicTypeDecl getDeclaration() {
		return declaration;
	}

	@Override
	public Availability getAvailability() {
		return availability;
	}

	public GlobalTypeDecl copy(String name, Availability availability, AlgebraicTypeDecl declaration) {
		if (Objects.equals(getName(), name) && Objects.equals(getAvailability(), availability) && Objects.equals(getDeclaration(), declaration)) {
			return this;
		} else {
			return new GlobalTypeDecl(this, name, availability, declaration);
		}
	}

	@Override
	public GlobalDecl withAvailability(Availability availability) {
		return copy(getName(), availability, getDeclaration());
	}

	@Override
	public Decl withName(String name) {
		return copy(name, getAvailability(), getDeclaration());
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(declaration);
	}

	@Override
	public Decl transformChildren(Transformation transformation) {
		return copy(getName(), getAvailability(), (AlgebraicTypeDecl) transformation.apply(getDeclaration()));
	}
}
