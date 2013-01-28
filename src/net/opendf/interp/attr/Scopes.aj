package net.opendf.interp.attr;

import java.util.BitSet;
import net.opendf.ir.am.Transition;
import net.opendf.ir.am.PredicateCondition;

public aspect Scopes {

	private interface RequiresVariables {}

	private interface InvalidatesVariables {}

	declare parents : Transition implements InvalidatesVariables;
	declare parents : (Transition || PredicateCondition) implements RequiresVariables;

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
