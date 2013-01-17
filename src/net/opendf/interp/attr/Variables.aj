package net.opendf.interp.attr;

import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.DeclVar;

public aspect Variables {
	
	private interface VarPos {}
	
	declare parents : (ExprVariable || StmtAssignment || DeclVar) implements VarPos;

	private int VarPos.position;
	private boolean VarPos.onStack;
	public int VarPos.getVariablePosition() {
		return position;
	}
	public void VarPos.setVariablePosition(int pos, boolean stack) {
		position = pos;
		onStack = stack;
	}
	public boolean VarPos.isVariableOnStack() {
		return onStack;
	}
}
