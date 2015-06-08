package se.lth.cs.tycho.backend.c;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.State;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class ScopeInitializationOld extends ScopeInitialization<State, Instruction> {
	private final ActorMachine actorMachine;
	private final ScopeDependencies scopeDependencies;

	public ScopeInitializationOld(ActorMachine actorMachine) {
		this.actorMachine = actorMachine;
		this.scopeDependencies = new ScopeDependencies(actorMachine);
		init();
	}

	protected List<Instruction> getInstructions(State s) {
		return s.getInstructions();
	}

	protected Collection<? extends State> getStates() {
		return actorMachine.getController();
	}

	protected BitSet killSet(Instruction i) {
		BitSet killSet;
		if (i instanceof ICall) {
			killSet = toBitSet(actorMachine.getTransition(((ICall) i).T()).getScopesToKill());
		} else {
			killSet = new BitSet();
		}
		return killSet;
	}

	private BitSet toBitSet(Collection<Integer> integers) {
		return integers.stream()
				.mapToInt(Integer::intValue)
				.collect(BitSet::new, BitSet::set, BitSet::or);
	}

	protected BitSet getDependencies(Instruction t) {
		return scopeDependencies.dependencies(t);
	}

	protected State[] targets(Instruction i) {
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

	protected int numberOfScopes() {
		return actorMachine.getScopes().size();
	}
}
