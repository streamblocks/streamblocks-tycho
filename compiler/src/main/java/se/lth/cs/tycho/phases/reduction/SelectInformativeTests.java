package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.InstructionVisitor;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;
import se.lth.cs.tycho.util.TychoCollectors;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class SelectInformativeTests implements Function<State, State> {
	@Override
	public State apply(State state) {
		return new MultiInstructionState(
				state.getInstructions().stream().collect(
						TychoCollectors.minimaBy(
								Comparator.comparingInt(new TransitionsFromTraget()),
								Collectors.toList()
						)
				)
		);
	}

	private static class TransitionsFromTraget implements InstructionVisitor<Integer, Void>, ToIntFunction<Instruction> {

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
		public int applyAsInt(Instruction instruction) {
			return instruction.accept(this);
		}
	}
}
