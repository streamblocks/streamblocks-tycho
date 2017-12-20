package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;

public abstract class ModuleInstanceDecl extends AbstractIRNode implements Decl {
	private final String name;
	private final String originalName;

	public ModuleInstanceDecl(ModuleInstanceDecl original, String name) {
		super(original);
		this.name = name;
		this.originalName = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getOriginalName() {
		return originalName;
	}

}
