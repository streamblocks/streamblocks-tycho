package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

abstract public class StructureStatement extends AttributableIRNode {
	public abstract <R, P> R accept(StructureStmtVisitor<R, P> v, P p);

	public StructureStatement(ImmutableList<ToolAttribute> toolAttributes) {
		super(toolAttributes);
	}
}
