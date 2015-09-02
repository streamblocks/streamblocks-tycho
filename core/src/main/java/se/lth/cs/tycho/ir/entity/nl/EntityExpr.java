package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public abstract class EntityExpr extends AbstractIRNode {
	public EntityExpr() {
		super(null);
	}

	public abstract <R, P> R accept(EntityExprVisitor<R, P> v, P p);
}
