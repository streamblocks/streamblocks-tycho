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

	@Override
	public TypeDecl transformChildren(Transformation transformation) {
		return this;
	}

	@Override
	public TypeDecl clone() {
		return (TypeDecl) super.clone();
	}

	@Override
	public TypeDecl deepClone() {
		return (TypeDecl) super.deepClone();
	}

	public Availability getAvailability() {
		return availability;
	}
}
