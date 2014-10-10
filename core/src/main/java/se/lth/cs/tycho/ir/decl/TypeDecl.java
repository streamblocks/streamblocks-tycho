package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

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
	
	@Override
	public DeclKind getKind() {
		return DeclKind.TYPE;
	}

}
