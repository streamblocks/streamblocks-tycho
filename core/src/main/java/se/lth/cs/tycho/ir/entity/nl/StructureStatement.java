package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AttributableIRNode;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

abstract public class StructureStatement extends AttributableIRNode {
	public abstract <R, P> R accept(StructureStmtVisitor<R, P> v, P p);
	public <R> R accept(StructureStmtVisitor<R, Void> v) {
		return accept(v, null);
	}

	public StructureStatement(StructureStatement original) {
		super(original);
	}

}
