package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

import java.util.function.Consumer;

public abstract class Decl extends AbstractIRNode {

	private final Availability availability;
	private final String name;
	private final DeclKind declKind;
	private final LocationKind locationKind;
	private final QID qualifiedIdentifier;

	protected Decl(IRNode original, LocationKind locationKind, Availability availability, DeclKind declKind, String name, QID qualifiedIdentifier) {
		super(original);
		this.availability = availability;
		this.name = name;
		this.declKind = declKind;
		this.locationKind = locationKind;
		this.qualifiedIdentifier = qualifiedIdentifier;
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

	public QID getQualifiedIdentifier() {
		return qualifiedIdentifier;
	}

	public boolean isImport() {
		return qualifiedIdentifier != null;
	}

}
