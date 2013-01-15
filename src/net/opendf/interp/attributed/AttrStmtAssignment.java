package net.opendf.interp.attributed;

import net.opendf.ir.common.StmtAssignment;

public class AttrStmtAssignment extends StmtAssignment implements VarLocation {
	private final boolean onStack;
	private final int varNumber;
	
	public AttrStmtAssignment(StmtAssignment base, int varNumber, boolean onStack) {
		super(base.getVar(), base.getVal(), base.getLocation(), base.getField());
		this.varNumber = varNumber;
		this.onStack = onStack;
	}

	@Override
	public boolean varOnStack() {
		return onStack;
	}

	@Override
	public int varPosition() {
		return varNumber;
	}
}
