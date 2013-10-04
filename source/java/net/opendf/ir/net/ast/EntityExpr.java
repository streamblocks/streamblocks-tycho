package net.opendf.ir.net.ast;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.util.ImmutableList;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public abstract class EntityExpr extends AbstractIRNode {
	public abstract <R, P> R accept(EntityExprVisitor<R, P> v, P p);

	public EntityExpr(EntityExpr original, ImmutableList<ToolAttribute> toolAttributes) {
		super(original, toolAttributes);
	}
}
