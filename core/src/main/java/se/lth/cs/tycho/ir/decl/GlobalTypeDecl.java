package se.lth.cs.tycho.ir.decl;

import java.util.Objects;

public class GlobalTypeDecl extends TypeDecl implements GlobalDecl {
	protected GlobalTypeDecl(TypeDecl original, Availability availability, String name) {
		super(original, availability, name);
	}

	@Override
	public GlobalTypeDecl withAvailability(Availability availability) {
		return getAvailability() == availability ? this : new GlobalTypeDecl(this, availability, getName());
	}

	@Override
	public AbstractDecl withName(String name) {
		return Objects.equals(getName(), name) ? this : new GlobalTypeDecl(this, getAvailability(), name);
	}
}
