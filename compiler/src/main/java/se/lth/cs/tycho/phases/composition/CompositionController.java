package se.lth.cs.tycho.phases.composition;

import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.am.ctrl.Controller;
import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;
import se.lth.cs.tycho.phases.cal2am.Knowledge;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompositionController implements Controller {

	private final List<ActorMachine> actorMachines;
	private final List<Connection> connections;
	private final CompositionState initialState;
	private final Map<CompositionState, CompositionState> cache;
	private final boolean eagerTest;

	public CompositionController(List<ActorMachine> actorMachines, List<Connection> connections, boolean eagerTest) {
		this.actorMachines = actorMachines;
		this.connections = connections;
		this.cache = new HashMap<>();
		this.initialState = state(actorMachines.stream().map(am -> am.controller().getInitialState()).toArray(State[]::new), new int[connections.size()]);
		this.eagerTest = eagerTest;
	}

	@Override
	public State getInitialState() {
		return initialState;
	}

	private int transitionIndex(int actor, int transition) {
		int offset = 0;
		for (int i = 0; i < actor; i++) {
			offset += actorMachines.get(i).getTransitions().size();
		}
		return offset + transition;
	}

	private int conditionIndex(int actor, int condition) {
		int offset = 0;
		for (int i = 0; i < actor; i++) {
			offset += actorMachines.get(i).getConditions().size();
		}
		return offset + condition;
	}


	private CompositionState state(State[] states, int[] tokens) {
		CompositionState state = new CompositionState(states, tokens);
		CompositionState cached = cache.get(state);
		if (cached == null) {
			cache.put(state, state);
			cached = state;
		}
		return cached;
	}


	public class CompositionState implements State {
		private final State[] states;
		private final int[] tokens;
		private State[] effectiveState; // Change to Instruction[]
		private List<Instruction> instructions;

		private CompositionState(State[] states, int[] tokens) {
			this.states = states;
			this.tokens = tokens;
		}

		private State[] effectiveState() {
			if (effectiveState == null) {
				effectiveState = new State[states.length];
				for (int a = 0; a < states.length; a++) {
					effectiveState[a] = effectiveState(a, states[a]);
				}
			}
			return effectiveState;
		}

		private State effectiveState(int actor, State state) {
			Instruction instruction = state.getInstructions().get(0);
			if (instruction instanceof Test) {
				Test test = ((Test) instruction);
				int conditionIndex = test.condition();
				Condition condition = actorMachines.get(actor).getConditions().get(conditionIndex);
				if (condition instanceof PortCondition) {
					PortCondition portCondition = (PortCondition) condition;
					Knowledge knowledge;
					if (portCondition.isInputCondition()) {
						knowledge = hasTokens(new TargetPort(actor, portCondition.getPortName().getName()), portCondition.N());
					} else {
						knowledge = hasSpace(new SourcePort(actor, portCondition.getPortName().getName()), portCondition.N());
					}
					switch (knowledge) {
						case UNKNOWN: return state;
						case TRUE: return effectiveState(actor, test.targetTrue());
						case FALSE: return effectiveState(actor, test.targetFalse());
					}
				}
			}
			return state;
		}


		private boolean isExecReachable(int actor, State state, Set<State> visited) {
			if (!visited.add(state)) {
				return false;
			}
			Instruction i = state.getInstructions().get(0);
			if (i instanceof Exec) {
				return true;
			} else if (i instanceof Test) {
				Test t = (Test) i;
				return isExecReachable(actor, effectiveState(actor, t.targetTrue()), visited)
						|| isExecReachable(actor, effectiveState(actor, t.targetFalse()), visited);
			} else {
				return false;
			}
		}


		private Knowledge hasSpace(SourcePort source, int n) {
			Knowledge result = Knowledge.UNKNOWN;
			for (int i = 0; i < connections.size(); i++) {
				Connection connection = connections.get(i);
				if (connection.getSource().equals(source)) {
					if (connection.getBufferSize() - tokens[i] < n) {
						return Knowledge.FALSE;
					} else {
						result = Knowledge.TRUE;
					}
				}
			}
			return result;
		}

		private Knowledge hasTokens(TargetPort target, int n) {
			for (int i = 0; i < connections.size(); i++) {
				Connection connection = connections.get(i);
				if (connection.getTarget().equals(target)) {
					return Knowledge.of(tokens[i] >= n);
				}
			}
			return Knowledge.UNKNOWN;
		}

		private Exec convert(int actor, Exec exec) {
			int[] newTokens = Arrays.copyOf(tokens, tokens.length);
			Transition transition = actorMachines.get(actor).getTransitions().get(exec.transition());
			for (int c = 0; c < connections.size(); c++) {
				Connection connection = connections.get(c);
				if (connection.getTarget().getActor() == actor) {
					newTokens[c] -= transition.getInputRate(new Port(connection.getTarget().getPort()));
				}
				if (connection.getSource().getActor() == actor) {
					newTokens[c] += transition.getOutputRate(new Port(connection.getSource().getPort()));
				}
			}
			State[] newStates = Arrays.copyOf(states, states.length);
			newStates[actor] = exec.target();
			return new Exec(transitionIndex(actor, exec.transition()), state(newStates, newTokens));
		}

		private Test convert(int actor, Test test) {
			State[] newStatesTrue = Arrays.copyOf(states, states.length);
			State[] newStatesFalse = Arrays.copyOf(states, states.length);
			newStatesTrue[actor] = test.targetTrue();
			newStatesFalse[actor] = test.targetFalse();
			return new Test(conditionIndex(actor, test.condition()), state(newStatesTrue, tokens), state(newStatesFalse, tokens));
		}

		private Wait generateWait() {
			State[] states = effectiveState();
			State[] newStates = Arrays.copyOf(states, states.length);
			for (int i = 0; i < newStates.length; i++) {
				Instruction instr = newStates[i].getInstructions().get(0);
				if (instr instanceof Wait) {
					Wait wait = (Wait) newStates[i].getInstructions().get(0);
					newStates[i] = wait.target();
				} else {
					newStates[i] = this.states[i];
				}
			}
			return new Wait(state(newStates, tokens), new BitSet());
		}


		private List<Instruction> computeInstructions() {
			State[] effectiveState = effectiveState();
			for (int actor = 0; actor < states.length; actor++) {
				Instruction i = effectiveState[actor].getInstructions().get(0);
				if (i instanceof Exec) {
					Instruction exec = convert(actor, (Exec) i);
					return Collections.singletonList(exec);
				}
			}
			for (int actor = 0; actor < states.length; actor++) {
				if (eagerTest || isExecReachable(actor, effectiveState[actor], new HashSet<>())) {
					Instruction i = effectiveState[actor].getInstructions().get(0);
					if (i instanceof Test) {
						Instruction test = convert(actor, (Test) i);
						return Collections.singletonList(test);
					}
				}
			}
			for (int actor = 0; actor < states.length; actor++) {
				Instruction i = effectiveState[actor].getInstructions().get(0);
				if (i instanceof Test) {
					Instruction test = convert(actor, (Test) i);
					return Collections.singletonList(test);
				}
			}
			return Collections.singletonList(generateWait());
		}

		@Override
		public List<Instruction> getInstructions() {
			if (instructions == null) {
				instructions = computeInstructions();
			}
			return instructions;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj instanceof CompositionState) {
				CompositionState that = (CompositionState) obj;
				return Arrays.equals(this.tokens, that.tokens) && Arrays.equals(this.states, that.states);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(tokens) * 31 + Arrays.hashCode(states);
		}
	}
}
