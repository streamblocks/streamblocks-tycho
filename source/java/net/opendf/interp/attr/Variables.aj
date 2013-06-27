package net.opendf.interp.attr;

import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ParDeclValue;

public aspect Variables {

	public interface VariablePosition {}

	public interface VariableDeclaration extends VariablePosition {}

	public interface VariableUse extends VariablePosition {}

	declare parents : (ExprVariable || StmtAssignment) implements VariableUse;
	declare parents : (DeclVar || ParDeclValue) implements VariableDeclaration;

	private int VariablePosition.position;
	private boolean VariablePosition.onStack;

	public int VariablePosition.getVariablePosition() {
		return position;
	}

	public void VariablePosition.setVariablePosition(int pos, boolean stack) {
		position = pos;
		onStack = stack;
	}

	public boolean VariablePosition.isVariableOnStack() {
		return onStack;
	}
}
