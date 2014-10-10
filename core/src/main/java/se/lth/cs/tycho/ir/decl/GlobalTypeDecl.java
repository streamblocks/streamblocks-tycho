package se.lth.cs.tycho.ir.decl;

import java.util.Objects;

public class GlobalTypeDecl extends TypeDecl implements GlobalDecl {

	private final Availability availability;

	public GlobalTypeDecl(String name, Availability availability) {
		this(null, name, availability);
	}

	private GlobalTypeDecl(GlobalTypeDecl original, String name, Availability availability) {
		super(original, name);
		this.availability = availability;
	}

	public GlobalTypeDecl copy(String name, Availability availability) {
		if (Objects.equals(getName(), name)) {
			return this;
		}
		return new GlobalTypeDecl(this, name, availability);
	}

	@Override
	public Availability getAvailability() {
		return availability;
	}

	@Override
	public <R, P> R accept(GlobalDeclVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalTypeDecl(this, param);
	}
}
