package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public interface StructureStatement extends IRNode {
	<R, P> R accept(StructureStmtVisitor<R, P> v, P p);
	default <R> R accept(StructureStmtVisitor<R, Void> v) {
		return accept(v, null);
	}
}
