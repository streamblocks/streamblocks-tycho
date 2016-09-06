package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.function.Consumer;

public abstract class Decl extends AbstractIRNode {

	private final Availability availability;
	private final String name;
	private final String originalName;
	private final DeclKind declKind;
	private final LocationKind locationKind;

	protected Decl(Decl original, LocationKind locationKind, Availability availability, DeclKind declKind, String name) {
		super(original);
		this.availability = availability;
		this.name = name;
		this.originalName = (original == null ? name : original.originalName);
		this.declKind = declKind;
		this.locationKind = locationKind;
	}

	public Availability getAvailability() {
		return availability;
	}

	public abstract Decl withAvailability(Availability availability);

	public String getName() {
		return name;
	}

	public String getOriginalName() { return originalName; }

	public DeclKind getDeclKind() {
		return declKind;
	}

	public LocationKind getLocationKind() {
		return locationKind;
	}

}
