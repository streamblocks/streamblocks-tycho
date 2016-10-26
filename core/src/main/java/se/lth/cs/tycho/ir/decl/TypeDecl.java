package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public abstract class TypeDecl extends AbstractDecl {

	private final Availability availability;

	protected TypeDecl(TypeDecl original, Availability availability, String name) {
		super(original, name);
		this.availability = availability;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
	}

	public Availability getAvailability() {
		return availability;
	}
}
