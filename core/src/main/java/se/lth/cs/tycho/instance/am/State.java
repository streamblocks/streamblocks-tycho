package se.lth.cs.tycho.instance.am;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

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
