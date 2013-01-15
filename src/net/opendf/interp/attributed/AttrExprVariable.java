package net.opendf.interp.attributed;

import net.opendf.ir.common.ExprVariable;

public class AttrExprVariable extends ExprVariable implements VarLocation {
	private final int varNumber;
	private final boolean onStack; 
	
	public AttrExprVariable(ExprVariable base, int varNumber, boolean onStack) {
		super(base.getName());
		this.varNumber = varNumber;
		this.onStack = onStack;
	}

	@Override
	public int varPosition() {
		return varNumber;
	}

	@Override
	public boolean varOnStack() {
		return onStack;
	}
}
