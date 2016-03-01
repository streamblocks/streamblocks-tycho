package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;

/**
 * 
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 * 
 */

public abstract class EntityExpr extends AttributableIRNode {
	public EntityExpr(EntityExpr original) {
		super(original);
	}

	public abstract <R, P> R accept(EntityExprVisitor<R, P> v, P p);

	@Override
	public EntityExpr deepClone() {
		return (EntityExpr) super.deepClone();
	}
}
