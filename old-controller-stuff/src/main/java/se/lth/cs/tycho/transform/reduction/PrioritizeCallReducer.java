package se.lth.cs.tycho.transform.reduction;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;

/**
 * State handler that filters the instructions by picking only Call instructions
 * if there is at least one.
 * 
 * @author gustav
 * 
 * @param <S>
 */
public class PrioritizeCallReducer<S> extends FilteredController<S> {
	public PrioritizeCallReducer(Controller<S> stateHandler) {
		super(stateHandler);
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		List<GenInstruction<S>> instructions = original.instructions(state);
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

	public static <S> Transformation<Controller<S>> transformation() {
		return (Controller<S> controller) -> new PrioritizeCallReducer<>(controller);
	}
}
