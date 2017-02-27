package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.State;

import java.util.function.Function;

public class SelectFirstInstruction implements Function<State, State> {
	@Override
	public State apply(State state) {
		return new SingleInstructionState(state.getInstructions().get(0));
	}
}
