package se.lth.cs.tycho.backend.c;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.State;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ScopeInitialization {
	private final ActorMachine actorMachine;
	private final ScopeDependencies scopeDependencies;
	private final Map<Instruction, BitSet> initSets;
	private final BitSet persistentScopes;

	public ScopeInitialization(ActorMachine actorMachine) {
		this.actorMachine = actorMachine;
		this.scopeDependencies = new ScopeDependencies(actorMachine);
		this.initSets = new HashMap<>();
		persistentScopes = new BitSet();
		init();
	}

	public BitSet scopesToInitialize(Instruction i) {
		BitSet init = initSets.get(i);
		return copy(init);
	}

	private void init() {
		Map<Instruction, BitSet> killSets = new HashMap<>();
		Map<State, Set<Instruction>> predecessors = new HashMap<>();

		persistentScopes.set(0, actorMachine.getScopes().size());
		for (State s : actorMachine.getController()) {
			for (Instruction i : s.getInstructions()) {
				if (i instanceof ICall) {
					BitSet killSet = toBitSet(actorMachine.getTransition(((ICall) i).T()).getScopesToKill());
					killSets.put(i, killSet);
					persistentScopes.andNot(killSet);
				} else {
					killSets.put(i, new BitSet());
				}
			}
		}

		for (State s : actorMachine.getController()) {
			for (Instruction i : s.getInstructions()) {
				for (State t : targets(i)) {
					predecessors.computeIfAbsent(t, z -> new HashSet()).add(i);
				}
			}
		}

		Map<Instruction, BitSet> outSets = new HashMap<>();

		Set<State> inQueue = new HashSet<>();
		Queue<State> queue = new ArrayDeque<>();

		queue.addAll(actorMachine.getController());
		inQueue.addAll(actorMachine.getController());

		while (!queue.isEmpty()) {
			State state = queue.remove();
			inQueue.remove(state);
			BitSet alive = predecessors.get(state).stream()
					.map(i -> outSets.getOrDefault(i, persistentScopes))
					.reduce(this::union)
					.orElse(persistentScopes);

			for (Instruction t : state.getInstructions()) {

				BitSet init = scopeDependencies.dependencies(t);
				init.andNot(alive);
				initSets.put(t, init);

				BitSet out = scopeDependencies.dependencies(t);
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

	private BitSet union(BitSet a, BitSet b) {
		BitSet result = new BitSet();
		result.or(a);
		result.and(b);
		return result;
	}


	private BitSet toBitSet(Collection<Integer> integers) {
		return integers.stream()
				.mapToInt(Integer::intValue)
				.collect(BitSet::new, BitSet::set, BitSet::or);
	}


	private BitSet copy(BitSet original) {
		BitSet copy = new BitSet();
		copy.or(original);
		return copy;
	}

	private State[] targets(Instruction i) {
		if (i instanceof ICall) {
			return new State[]{
					actorMachine.getController().get(((ICall) i).S())
			};
		} else if (i instanceof ITest) {
			return new State[]{
					actorMachine.getController().get(((ITest) i).S0()),
					actorMachine.getController().get(((ITest) i).S1())
			};
		} else if (i instanceof IWait) {
			return new State[]{
					actorMachine.getController().get(((IWait) i).S())
			};
		} else {
			throw new Error();
		}
	}

	public BitSet persistentScopes() {
		return copy(persistentScopes);
	}
}
