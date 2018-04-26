package se.lth.cs.tycho.ir.stmt.lvalue;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

/**
 * LValue represent things that can be on the left hand side of an assignment.
 */
public abstract class LValue extends AbstractIRNode {
	public LValue(IRNode original) {
		super(original);
	}

	@Override
	public abstract LValue transformChildren(Transformation transformation);
}
