package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.util.TychoCollectors;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SelectFatalTests implements Function<State, State> {
	@Override
	public State apply(State state) {
		return new MultiInstructionState(
				state.getInstructions().stream().collect(
						TychoCollectors.minimaBy(
								Comparator.comparingInt(this::nbrOfInstrsInFalseBranch),
								Collectors.toList())));
	}

	private int nbrOfInstrsInFalseBranch(Instruction i) {
		return i.accept(
				exec -> 0,
				test -> test.targetFalse().getInstructions().size(),
				wait -> 0);
	}
}
