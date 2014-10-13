package se.lth.cs.tycho.transform.reduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.ActorMachineState;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class TransitionPriorityStateHandler<S> implements ActorMachineState<S> {
	private final ActorMachineState<S> actorMachineState;
	private final Selector<Integer> transitionSelector;
	private final Map<S, List<GenInstruction<S>>> decisions;

	public TransitionPriorityStateHandler(ActorMachineState<S> stateHandler, Selector<Integer> transitionSelector) {
		this.actorMachineState = stateHandler;
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
			List<GenInstruction<S>> shouldBeWait = actorMachineState.getInstructions(state);
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
		for(GenInstruction<S> instruction : actorMachineState.getInstructions(state)) {
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
			for (GenInstruction<S> instruction : actorMachineState.getInstructions(state)) {
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
		return actorMachineState.initialState();
	}

}
