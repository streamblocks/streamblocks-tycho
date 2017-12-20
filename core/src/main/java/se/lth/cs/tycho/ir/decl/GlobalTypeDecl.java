package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

public class GlobalTypeDecl extends TypeDecl implements GlobalDecl {
	private final Availability availability;

	protected GlobalTypeDecl(TypeDecl original, Availability availability, String name) {
		super(original, name);
		this.availability = availability;
	}

	@Override
	public GlobalTypeDecl withName(String name) {
		return Objects.equals(getName(), name) ? this : new GlobalTypeDecl(this, getAvailability(), name);
	}

	public Availability getAvailability() {
		return availability;
	}

	@Override
	public GlobalTypeDecl withAvailability(Availability availability) {
		return getAvailability() == availability ? this : new GlobalTypeDecl(this, availability, getName());
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}

	@Override
	public GlobalTypeDecl transformChildren(Transformation transformation) {
		return this;
	}
}
