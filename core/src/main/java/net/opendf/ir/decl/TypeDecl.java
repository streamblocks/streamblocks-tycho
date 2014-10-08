package net.opendf.ir.decl;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;

public abstract class TypeDecl extends AbstractIRNode implements Decl {

	private final String name;

	public TypeDecl(IRNode original, String name) {
		super(original);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
