package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;

public abstract class AbstractDecl<This extends AbstractDecl<This>> extends AbstractIRNode implements Decl<This> {

	private final String name;
	private final String originalName;

	protected AbstractDecl(AbstractDecl original, String name) {
		super(original);
		this.name = name;
		this.originalName = (original == null ? name : original.originalName);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getOriginalName() { return originalName; }

	@Override
	public final This clone() {
		return (This) super.clone();
	}

	@Override
	public final This deepClone() {
		return (This) super.deepClone();
	}

}
