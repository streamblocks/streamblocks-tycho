package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class SelectRandom implements Function<State, State> {
	private final Random random;

	public SelectRandom(long seed) {
		this.random = new Random(seed);
	}
	@Override
	public State apply(State state) {
		List<Instruction> instructions = state.getInstructions();
		int index = random.nextInt(instructions.size());
		Instruction instruction = instructions.get(index);
		return new SingleInstructionState(instruction);
	}
}
