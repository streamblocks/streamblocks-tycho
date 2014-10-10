package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeExpr;

public abstract class VarDecl extends AbstractIRNode implements Decl {

	private final String name;
	private final TypeExpr type;

	public TypeExpr getType() {
		return type;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public DeclKind getKind() {
		return DeclKind.VAR;
	}

	public VarDecl(IRNode original, TypeExpr type, String name) {
		super(original);
		this.name = name;
		this.type = type;
	}

}