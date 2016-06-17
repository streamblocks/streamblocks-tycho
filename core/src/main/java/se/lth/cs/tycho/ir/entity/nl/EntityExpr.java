package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public interface EntityExpr extends IRNode {
	<R, P> R accept(EntityExprVisitor<R, P> v, P p);
	default <R> R accept(EntityExprVisitor<R, Void> v) {
		return accept(v, null);
	}
}
