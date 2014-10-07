package net.opendf.ir.entity.nl;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

abstract public class StructureStatement extends AbstractIRNode {
	public abstract <R, P> R accept(StructureStmtVisitor<R, P> v, P p);

	public StructureStatement(IRNode original, ImmutableList<ToolAttribute> toolAttributes) {
		super(original, toolAttributes);
	}
}
