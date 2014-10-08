package net.opendf.ir.entity.am;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class State extends AbstractIRNode {
	private final ImmutableList<Instruction> instructions;

	public State(ImmutableList<Instruction> instructions) {
		this(null, instructions);
	}

	private State(IRNode original, ImmutableList<Instruction> instructions) {
		super(original);
		this.instructions = ImmutableList.copyOf(instructions);
	}

	public State copy(ImmutableList<Instruction> instructions) {
		if (Lists.equals(this.instructions, instructions)) {
			return this;
		}
		return new State(this, instructions);
	}

	public ImmutableList<Instruction> getInstructions() {
		return instructions;
	}

}
