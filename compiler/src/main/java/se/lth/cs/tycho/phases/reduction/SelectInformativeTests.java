package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.InstructionVisitor;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public class SelectInformativeTests implements Function<State, State> {
	@Override
	public State apply(State state) {
		Optional<Instruction> min = state.getInstructions().stream()
				.min(Comparator.comparing(new TransitionsFromTraget()));
		return new SingleInstructionState(min.get());
	}

	private static class TransitionsFromTraget implements InstructionVisitor<Integer, Void>, Function<Instruction, Integer> {

		@Override
		public Integer visitExec(Exec t, Void aVoid) {
			return 0;
		}

		@Override
		public Integer visitTest(Test t, Void aVoid) {
			return t.targetTrue().getInstructions().size() + t.targetFalse().getInstructions().size();
		}

		@Override
		public Integer visitWait(Wait t, Void aVoid) {
			return 0;
		}

		@Override
		public Integer apply(Instruction instruction) {
			return instruction.accept(this);
		}
	}
}
