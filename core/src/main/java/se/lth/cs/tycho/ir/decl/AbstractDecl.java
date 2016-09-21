package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;

public abstract class AbstractDecl extends AbstractIRNode implements Decl {

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
	public abstract AbstractDecl withName(String name);

	@Override
	public String getOriginalName() { return originalName; }

}
