package net.opendf.ir.net.ast;

import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;

/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public class StructureConnectionStmt extends StructureStatement {

	public StructureConnectionStmt(PortReference src, PortReference dst, ImmutableList<ToolAttribute> toolAttributes){
		this.src = src;
		this.dst = dst;
		this.toolAttributes = ImmutableList.copyOf(toolAttributes);
	}

	public PortReference getSrc(){
		return src;
	}
	public PortReference getDst(){
		return dst;
	}
	public ImmutableList<ToolAttribute> getToolAttributes(){
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
	protected ImmutableList<ToolAttribute> toolAttributes;
}
