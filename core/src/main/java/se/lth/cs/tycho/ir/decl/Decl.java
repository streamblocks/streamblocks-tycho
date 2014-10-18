package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

public abstract class Decl extends AbstractIRNode {

	private final Availability availability;
	private final String name;
	private final DeclKind declKind;
	private final LocationKind locationKind;

	protected Decl(IRNode original, LocationKind locationKind, Availability availability, DeclKind declKind, String name) {
		super(original);
		this.availability = availability;
		this.name = name;
		this.declKind = declKind;
		this.locationKind = locationKind;
	}

	public Availability getAvailability() {
		return availability;
	}

	public String getName() {
		return name;
	}

	public DeclKind getDeclKind() {
		return declKind;
	}

	public LocationKind getLocationKind() {
		return locationKind;
	}

}
