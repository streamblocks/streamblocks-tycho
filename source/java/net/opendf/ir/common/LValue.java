package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

/**
 * LValue represent things that can be on the left hand side of an assignment.
 */
public abstract class LValue extends AbstractIRNode {
	public abstract <R, P> R accept(LValueVisitor<R, P> visitor, P parameter);

	public <R> R accept(LValueVisitor<R, Void> visitor) {
		return accept(visitor, null);
	}
	
	public LValue(LValue original) {
		super(original);
	}
}
