package se.lth.cs.tycho.backend.c;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ctrl.Exec;
import se.lth.cs.tycho.instance.am.ctrl.State;
import se.lth.cs.tycho.instance.am.ctrl.Test;
import se.lth.cs.tycho.instance.am.ctrl.Transition;
import se.lth.cs.tycho.instance.am.ctrl.TransitionVisitor;
import se.lth.cs.tycho.instance.am.ctrl.Wait;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class ScopeInitializationNew extends ScopeInitialization<State, Transition> {
	private final ActorMachine actorMachine;
	private final ScopeDependencies scopeDependencies;

	public ScopeInitializationNew(ActorMachine actorMachine) {
		this.actorMachine = actorMachine;
		this.scopeDependencies = new ScopeDependencies(actorMachine);
		init();
	}

	@Override
	protected int numberOfScopes() {
		return actorMachine.getScopes().size();
	}

	@Override
	protected BitSet getDependencies(Transition t) {
		return scopeDependencies.dependencies(t);
	}

	@Override
	protected List<Transition> getInstructions(State s) {
		return s.getTransitions();
	}

	@Override
	protected Collection<? extends State> getStates() {
		return actorMachine.controller().getAllStates();
	}

	@Override
	protected BitSet killSet(Transition i) {
		return i.accept(new TransitionVisitor<BitSet, Void>() {
			@Override
			public BitSet visitExec(Exec t, Void aVoid) {
				return actorMachine.getTransition(t.transition()).getScopesToKill().stream()
						.mapToInt(Integer::intValue)
						.collect(BitSet::new, BitSet::set, BitSet::or);
			}

			@Override
			public BitSet visitTest(Test t, Void aVoid) {
				return new BitSet();
			}

			@Override
			public BitSet visitWait(Wait t, Void aVoid) {
				return new BitSet();
			}
		});
	}

	@Override
	protected State[] targets(Transition i) {
		return i.targets();
	}
}
