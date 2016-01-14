package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;

import java.util.Collections;
import java.util.List;

public class SingleInstructionState implements State {
	private final List<Instruction> instruction;

	public SingleInstructionState(Instruction i) {
		if (i == null) {
			throw new IllegalArgumentException();
		} else {
			instruction = Collections.singletonList(i);
		}
	}

	@Override
	public List<Instruction> getInstructions() {
		return instruction;
	}
}
