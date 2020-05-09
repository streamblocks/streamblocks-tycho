package se.lth.cs.tycho.ir.decl;

public abstract class GlobalTypeDecl extends TypeDecl implements GlobalDecl {

	private final Availability availability;

	public GlobalTypeDecl(TypeDecl original, String name, Availability availability) {
		super(original, name);
		this.availability = availability;
	}

	@Override
	public Availability getAvailability() {
		return availability;
	}
}