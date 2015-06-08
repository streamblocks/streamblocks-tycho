package se.lth.cs.tycho.backend.c;

import se.lth.cs.tycho.instance.am.ActorMachine;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public abstract class ScopeInitialization<State, Instruction> {
	private final Map<Instruction, BitSet> initSets;
	private final BitSet persistentScopes;

	public ScopeInitialization() {
		this.initSets = new HashMap<>();
		persistentScopes = new BitSet();
	}

	public BitSet scopesToInitialize(Instruction i) {
		BitSet init = initSets.get(i);
		return copy(init);
	}

	protected void init() {
		Map<Instruction, BitSet> killSets = new HashMap<>();
		Map<State, Set<Instruction>> predecessors = new HashMap<>();

		persistentScopes.set(0, numberOfScopes());
		for (State s : getStates()) {
			for (Instruction i : getInstructions(s)) {
				BitSet killSet;
				killSet = killSet(i);
				killSets.put(i, killSet);
				persistentScopes.andNot(killSet);
			}
		}

		for (State s : getStates()) {
			for (Instruction i : getInstructions(s)) {
				for (State t : targets(i)) {
					predecessors.computeIfAbsent(t, z -> new HashSet()).add(i);
				}
			}
		}

		Map<Instruction, BitSet> outSets = new HashMap<>();

		Set<State> inQueue = new HashSet<>();
		Queue<State> queue = new ArrayDeque<>();

		queue.addAll(getStates());
		inQueue.addAll(getStates());

		while (!queue.isEmpty()) {
			State state = queue.remove();
			inQueue.remove(state);
			BitSet alive = predecessors.get(state).stream()
					.map(i -> outSets.getOrDefault(i, persistentScopes))
					.reduce(this::union)
					.orElse(persistentScopes);

			for (Instruction t : getInstructions(state)) {

				BitSet init = getDependencies(t);
				init.andNot(alive);
				initSets.put(t, init);

				BitSet out = getDependencies(t);
				out.or(alive);
				out.andNot(killSets.get(t));

				if (!out.equals(outSets.getOrDefault(t, persistentScopes))) {
					outSets.put(t, out);
					for (State s : targets(t)) {
						if (!inQueue.contains(s)) {
							inQueue.add(s);
							queue.add(s);
						}
					}
				}
			}
		}
	}

	protected abstract int numberOfScopes();

	protected abstract BitSet getDependencies(Instruction t);

	protected abstract List<Instruction> getInstructions(State s);

	protected abstract Collection<? extends State> getStates();

	protected abstract BitSet killSet(Instruction i);

	private BitSet union(BitSet a, BitSet b) {
		BitSet result = new BitSet();
		result.or(a);
		result.and(b);
		return result;
	}


	private BitSet copy(BitSet original) {
		BitSet copy = new BitSet();
		copy.or(original);
		return copy;
	}

	protected abstract State[] targets(Instruction i);

	public BitSet persistentScopes() {
		return copy(persistentScopes);
	}
}
