package se.lth.cs.tycho.transform.filter;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.ActorMachineState;

/**
 * State handler that filters the instructions by picking only Call instructions
 * if there is at least one.
 * 
 * @author gustav
 * 
 * @param <S>
 */
public class PrioritizeCallInstructions<S> implements ActorMachineState<S> {
	private final ActorMachineState<S> actorMachineState;

	public PrioritizeCallInstructions(ActorMachineState<S> stateHandler) {
		this.actorMachineState = stateHandler;
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		List<GenInstruction<S>> instructions = actorMachineState.getInstructions(state);
		for (GenInstruction<S> instr : instructions) {
			if (instr.isCall()) {
				return getCalls(instructions);
			}
		}
		return instructions;
	}

	private List<GenInstruction<S>> getCalls(List<GenInstruction<S>> instructions) {
		List<GenInstruction<S>> result = new ArrayList<>(1);
		for (GenInstruction<S> instr : instructions) {
			if (instr.isCall()) {
				result.add(instr);
			}
		}
		return result;
	}

	@Override
	public S initialState() {
		return actorMachineState.initialState();
	}
}
