package se.lth.cs.tycho.ir.stmt.lvalue;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

/**
 * LValue represent things that can be on the left hand side of an assignment.
 */
public abstract class LValue extends AbstractIRNode {
	public abstract <R, P> R accept(LValueVisitor<R, P> visitor, P parameter);

	public <R> R accept(LValueVisitor<R, Void> visitor) {
		return accept(visitor, null);
	}

	public LValue(IRNode original) {
		super(original);
	}

	@Override
	public abstract LValue transformChildren(Transformation transformation);
}
