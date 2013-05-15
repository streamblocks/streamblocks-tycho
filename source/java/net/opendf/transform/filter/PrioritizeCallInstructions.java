package net.opendf.transform.filter;

import java.util.ArrayList;
import java.util.List;

import net.opendf.transform.util.GenInstruction;
import net.opendf.transform.util.StateHandler;

/**
 * State handler that filters the instructions by picking only Call instructions
 * if there is at least one.
 * 
 * @author gustav
 * 
 * @param <S>
 */
public class PrioritizeCallInstructions<S> implements StateHandler<S> {
	private final StateHandler<S> stateHandler;

	public PrioritizeCallInstructions(StateHandler<S> stateHandler) {
		this.stateHandler = stateHandler;
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		List<GenInstruction<S>> instructions = stateHandler.getInstructions(state);
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
		return stateHandler.initialState();
	}

	public static <S> InstructionFilterFactory<S> getFactory() {
		return new InstructionFilterFactory<S>() {
			public StateHandler<S> createFilter(StateHandler<S> stateHandler) {
				return new PrioritizeCallInstructions<S>(stateHandler);
			}
		};
	}
}
