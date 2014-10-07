package net.opendf.ir.decl;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.TypeExpr;

public abstract class VarDecl extends AbstractIRNode implements Decl {

	private final String name;
	private final TypeExpr type;

	public TypeExpr getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}

	public VarDecl(IRNode original, TypeExpr type, String name) {
		super(original);
		this.name = name;
		this.type = type;
	}

}