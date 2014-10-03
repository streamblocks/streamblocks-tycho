package net.opendf.transform.reduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.opendf.transform.util.GenInstruction;
import net.opendf.transform.util.GenInstruction.Call;
import net.opendf.transform.util.GenInstruction.Test;
import net.opendf.transform.util.GenInstruction.Wait;
import net.opendf.transform.util.StateHandler;

public class TransitionPriorityStateHandler<S> implements StateHandler<S> {
	private final StateHandler<S> stateHandler;
	private final Selector<Integer> transitionSelector;
	private final Map<S, List<GenInstruction<S>>> decisions;

	public TransitionPriorityStateHandler(StateHandler<S> stateHandler, Selector<Integer> transitionSelector) {
		this.stateHandler = stateHandler;
		this.transitionSelector = transitionSelector;
		decisions = new HashMap<>();
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		if (!decisions.containsKey(state)) {
			Set<Integer> transitions = getReachableTransitions(state);
			Integer transition = transitionSelector.select(transitions);
			if (transition != null) addPathsToTransition(state, transition);
		}
		List<GenInstruction<S>> instructions = decisions.get(state);
		if (instructions == null || instructions.isEmpty()) {
			List<GenInstruction<S>> shouldBeWait = stateHandler.getInstructions(state);
			switch (shouldBeWait.size()) {
			case 0: return shouldBeWait;
			case 1: if (shouldBeWait.get(0) instanceof Wait) return shouldBeWait;
			}
			throw new Error("Expected a single wait instruction or nothing.");
		} else {
			return instructions;
		}
	}

	private boolean addPathsToTransition(S state, int transition) {
		boolean leadsToTransition = false;
		Set<GenInstruction<S>> localDecisions = new LinkedHashSet<>();
		for(GenInstruction<S> instruction : stateHandler.getInstructions(state)) {
			if (instruction instanceof Call) {
				Call<S> call = (Call<S>) instruction;
				if (call.T() == transition) {
					localDecisions.add(call);
					leadsToTransition = true;
				}
			} else if (instruction instanceof Test) {
				for (S destination : instruction.destinations()) {
					if (decisions.containsKey(destination) || addPathsToTransition(destination, transition)) {
						localDecisions.add(instruction);
						leadsToTransition = true;
					}
				}
			} else if (instruction instanceof Wait) {
				localDecisions.add(instruction);
			}
		}
		if (leadsToTransition) decisions.put(state, new ArrayList<>(localDecisions));
		return leadsToTransition;
	}

	private Set<Integer> getReachableTransitions(S sourceState) {
		Set<Integer> result = new HashSet<>();
		Set<S> visited = new HashSet<>();
		Queue<S> queue = new LinkedList<>();
		queue.add(sourceState);
		visited.add(sourceState);
		while (!queue.isEmpty()) {
			S state = queue.remove();
			for (GenInstruction<S> instruction : stateHandler.getInstructions(state)) {
				if (instruction instanceof Call) {
					result.add(((Call<S>) instruction).T());
				} else if (instruction.isTest()) {
					for (S destination : instruction.destinations()) {
						if (visited.add(destination)) {
							queue.add(destination);
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public S initialState() {
		return stateHandler.initialState();
	}

}
