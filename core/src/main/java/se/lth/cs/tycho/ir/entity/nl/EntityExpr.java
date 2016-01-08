package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public abstract class EntityExpr extends AttributableIRNode {
	public EntityExpr(ImmutableList<ToolAttribute> toolAttributes) {
		super(toolAttributes);
	}

	public abstract <R, P> R accept(EntityExprVisitor<R, P> v, P p);
}
