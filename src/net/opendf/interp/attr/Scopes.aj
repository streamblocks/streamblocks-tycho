package net.opendf.interp.attr;

import java.util.BitSet;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;

public aspect Scopes {
	
	private interface RequiresVariables {}
	private interface InvalidatesVariables {}
	
	declare parents : ICall implements InvalidatesVariables;
	declare parents : (ICall || ITest) implements RequiresVariables;
	
	private BitSet RequiresVariables.requiredVariables;
	private BitSet InvalidatesVariables.invalidatedVariables;

	public BitSet RequiresVariables.getRequiredVariables() {
		return requiredVariables;
	}
	
	public void RequiresVariables.setRequiredVariables(BitSet vars) {
		requiredVariables = vars;
	}
	
	public BitSet InvalidatesVariables.getInvalidatedVariables() {
		return invalidatedVariables;
	}
	
	public void InvalidatesVariables.setInvalidatedVariables(BitSet vars) {
		invalidatedVariables = vars;
	}
}
