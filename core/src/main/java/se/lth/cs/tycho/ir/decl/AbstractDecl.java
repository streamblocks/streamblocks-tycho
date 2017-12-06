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
	public String getOriginalName() { return originalName; }

	@Override
	public AbstractDecl clone() {
		return (AbstractDecl) super.clone();
	}

	@Override
	public AbstractDecl deepClone() {
		return (AbstractDecl) super.deepClone();
	}

}
