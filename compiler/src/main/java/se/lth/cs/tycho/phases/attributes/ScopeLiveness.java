package se.lth.cs.tycho.phases.attributes;

import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.util.BitSets;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public class ScopeLiveness {
	private final ActorMachineScopes scopes;
	private final ActorMachine actorMachine;
	private final HashMap<Instruction, State> sourceState;
	private final HashMap<State, List<Instruction>> incoming;
	private final HashMap<State, BitSet> alive;

	public ScopeLiveness(ActorMachineScopes scopes, ActorMachine actorMachine) {
		this.scopes = scopes;
		this.actorMachine = actorMachine;
		this.sourceState = new HashMap<>();
		this.incoming = new HashMap<>();
		this.alive = new HashMap<>();
		initLinks();
		computeLiveness();
	}

	private void initLinks() {
		for (State s : actorMachine.controller().getStateList()) {
			for (Instruction i : s.getInstructions()) {
				sourceState.put(i, s);
				for (State t : targetStates(i)) {
					incoming.computeIfAbsent(t, x -> new ArrayList<>()).add(i);
				}
			}
		}
	}

	private void computeLiveness() {
		Queue<State> states = new ArrayDeque<>();
		states.addAll(actorMachine.controller().getStateList());
		while (!states.isEmpty()) {
			State s = states.remove();
			BitSet current = currentAlive(s);
			BitSet computed = computeAlive(s);
			if (!current.equals(computed)) {
				alive.put(s, computed);
				for (Instruction i : outgoing(s)) {
					for (State t : targetStates(i)) {
						states.add(t);
					}
				}
			}
		}
	}

	public BitSet init(Instruction i) {
		BitSet aliveSource = currentAlive(sourceState(i));
		BitSet required = scopes.required(actorMachine, i);
		return BitSets.difference(required, aliveSource);
	}

	private BitSet persistentScopes() {
		return scopes.persistentScopes(actorMachine);
	}

	private BitSet transientScopes() {
		return scopes.transientScopes(actorMachine);
	}

	private BitSet kill(Instruction i) {
		return i.accept(
				exec -> transientScopes(),
				test -> new BitSet(),
				wait -> new BitSet());
	}

	private BitSet aliveOut(Instruction i) {
		BitSet aliveSource = currentAlive(sourceState(i));
		BitSet required = scopes.required(actorMachine, i);
		BitSet kill = kill(i);
		return BitSets.difference(BitSets.union(aliveSource, required), kill);
	}

	private BitSet currentAlive(State s) {
		return alive.computeIfAbsent(s, x -> persistentScopes());
	}

	private BitSet computeAlive(State s) {
		return incoming(s).stream()
				.map(this::aliveOut)
				.reduce(BitSets::intersection)
				.orElse(persistentScopes());
	}

	private List<Instruction> incoming(State targetState) {
		return incoming.getOrDefault(targetState, ImmutableList.empty());
	}

	private List<Instruction> outgoing(State sourceState) {
		return sourceState.getInstructions();
	}

	private State sourceState(Instruction instruction) {
		State source = sourceState.get(instruction);
		assert source != null;
		return source;
	}

	private List<State> targetStates(Instruction instruction) {
		return instruction.accept(
				exec -> ImmutableList.of(exec.target()),
				test -> ImmutableList.of(test.targetTrue(), test.targetFalse()),
				wait -> ImmutableList.of(wait.target()));
	}
}
