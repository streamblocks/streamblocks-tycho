package net.opendf.ir.net.ast;

import net.opendf.ir.AbstractIRNode;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

abstract public class StructureStatement extends AbstractIRNode {
	public abstract <R, P> R accept(StructureStmtVisitor<R, P> v, P p);

	public StructureStatement(StructureStatement original) {
		super(original);
	}
}
