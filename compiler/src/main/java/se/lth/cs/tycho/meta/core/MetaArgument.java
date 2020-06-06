package se.lth.cs.tycho.meta.core;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

public abstract class MetaArgument extends AbstractIRNode {

	private final String name;

	public MetaArgument(IRNode original, String name) {
		super(original);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
