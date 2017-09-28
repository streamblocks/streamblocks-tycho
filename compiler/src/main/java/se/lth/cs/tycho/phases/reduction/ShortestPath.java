package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.InstructionKind;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.util.TychoCollectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShortestPath implements Function<State, State> {
	private Map<Instruction, Integer> distances;

	public ShortestPath() {
		this.distances = new HashMap<>();
	}

	public State apply(State state) {
		{
			List<Instruction> exec = getInstructionsOfKind(state, InstructionKind.EXEC);
			if (!exec.isEmpty()) {
				return new MultiInstructionState(exec);
			}
		}
		{
			List<Instruction> wait = getInstructionsOfKind(state, InstructionKind.WAIT);
			if (!wait.isEmpty()) {
				return new MultiInstructionState(wait);
			}
		}
		if (!state.getInstructions().stream().anyMatch(distances::containsKey)) {
			distances.putAll(computeDistances(state));
		}
		List<Instruction> closest = state.getInstructions().stream().collect(
				TychoCollectors.minimaBy(
						Comparator.comparingInt(this::getDistance),
						Collectors.toList()));
		return new MultiInstructionState(closest);
	}

	private int getDistance(Instruction i) {
		return distances.getOrDefault(i, 1000);
	}

	private Map<Instruction, Integer> computeDistances(State state) {
		Map<Instruction, State> source = new HashMap<>();
		Map<State, List<Instruction>> incoming = new HashMap<>();
		List<State> goalStates = new ArrayList<>();
		Map<Instruction, Integer> distance = new HashMap<>();
		{
			Set<State> added = new HashSet<>();
			added.add(state);
			Queue<State> queue = new ArrayDeque<>();
			queue.add(state);
			while (!queue.isEmpty()) {
				if (added.size() > 100000) {
					return added.stream()
							.flatMap(s -> s.getInstructions().stream())
							.collect(Collectors.toMap(Function.identity(), x -> 1000));
				}
				State s = queue.remove();
				for (Instruction i : s.getInstructions()) {
					source.put(i, s);
					i.forEachTarget(t -> incoming.computeIfAbsent(t, x -> new ArrayList<>()).add(i));
					if (i instanceof Exec) {
						distance.put(i, 0);
					} else if (i instanceof Test) {
						i.forEachTarget(t -> {
							if (added.add(t)) {
								queue.add(t);
							}
						});
					}
				}
			}
		}
		{
			List<State> currentFrontier = goalStates;
			int currentDistance = 1;
			while (!currentFrontier.isEmpty()) {
				List<State> nextFrontier = new ArrayList<>();
				for (State s : currentFrontier) {
					for (Instruction i : incoming.getOrDefault(s, Collections.emptyList())) {
						if (!distance.containsKey(i)) {
							distance.put(i, currentDistance);
							nextFrontier.add(source.get(i));
						}
					}
				}
				currentFrontier = nextFrontier;
				currentDistance++;
			}
		}
		return distance;
	}

	private List<Instruction> getInstructionsOfKind(State s, InstructionKind kind) {
		return s.getInstructions().stream()
				.filter(i -> i.getKind() == kind)
				.collect(Collectors.toList());
	}

}
