package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ShortestPathToExec implements Function<State, State> {
	@Override
	public State apply(State state) {
		return new SingleInstructionState(get(state));
	}

	private Instruction get(State s) {

		Map<State, Instruction> nextLevel = new HashMap<>();
		for (Instruction i : s.getInstructions()) {
			if (i instanceof Exec) {
				return i;
			} else if (i instanceof Test) {
				Test t = (Test) i;
				nextLevel.put(t.targetTrue(), t);
				nextLevel.put(t.targetFalse(), t);
			}
		}
		Instruction result;
		if (nextLevel.isEmpty()) {
			result = s.getInstructions().get(0);
		} else {
			result = minLength(nextLevel);
		}
		return result;
	}

	private Instruction minLength(Map<State, Instruction> states) {
		Map<State, Instruction> nextLevel = new HashMap<>();
		Map<Instruction, State> origin = new HashMap<>();
		for (State state : states.keySet()) {
			for (Instruction i : state.getInstructions()) {
				if (i instanceof Exec) {
					return states.get(state);
				} else if (i instanceof Test) {
					Test t = (Test) i;
					nextLevel.put(t.targetTrue(), t);
					nextLevel.put(t.targetFalse(), t);
					origin.put(t, state);
				}
			}
		}
		Instruction result;
		if (nextLevel.isEmpty()) {
			result = states.values().iterator().next();
		} else {
			State state = origin.get(minLength(nextLevel));
			result = states.get(state);
		}
		return result;
	}

}
