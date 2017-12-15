package se.lth.cs.tycho.transformation.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;

import java.util.List;

public class MultiInstructionState implements State {
	private final List<Instruction> instructions;

	public MultiInstructionState(List<Instruction> instructions) {
		this.instructions = instructions;
	}

	@Override
	public List<Instruction> getInstructions() {
		return instructions;
	}
}
