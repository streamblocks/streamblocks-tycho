package net.opendf.ir.net.ast;

import net.opendf.ir.net.ToolAttribute;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class StructureConnectionStmt extends StructureStatement {

	public StructureConnectionStmt(PortReference src, PortReference dst, ToolAttribute[] toolAttributes){
		this.src = src;
		this.dst = dst;
		this.toolAttributes = toolAttributes;
	}

	public PortReference getSrc(){
		return src;
	}
	public PortReference getDst(){
		return dst;
	}
	public ToolAttribute[] getToolAttributes(){
		return toolAttributes;
	}

	public String toString(){
		return src.toString() + " --> " + dst.toString();
	}

	@Override
	public <R, P> R accept(StructureStmtVisitor<R, P> v, P p) {
		return v.visitStructureConnectionStmt(this, p);
	}
	private PortReference src, dst;
	protected ToolAttribute[] toolAttributes;
}
