package net.opendf.interp.attributed;

import net.opendf.ir.common.DeclVar;

public class AttrDeclVar extends DeclVar implements VarLocation {
	private final int varNumber;
	private final boolean onStack;
	
	public AttrDeclVar(DeclVar base, int varNumber, boolean onStack) {
		super(base.getType(), base.getName(), base.getNamespaceDecl(), base.getInitialValue(), base.isAssignable());
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
