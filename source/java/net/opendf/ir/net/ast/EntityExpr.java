package net.opendf.ir.net.ast;

import net.opendf.ir.AbstractIRNode;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public abstract class EntityExpr extends AbstractIRNode {
	public abstract <R, P> R accept(EntityExprVisitor<R, P> v, P p);

	public EntityExpr(EntityExpr original) {
		super(original);
	}
}
