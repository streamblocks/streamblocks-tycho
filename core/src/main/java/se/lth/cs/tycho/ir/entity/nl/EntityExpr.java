package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;

/**
 * 
 * @author Per Andersson
 * 
 */

public interface EntityExpr extends IRNode {
    <R, P> R accept(EntityExprVisitor<R, P> v, P p);
}
