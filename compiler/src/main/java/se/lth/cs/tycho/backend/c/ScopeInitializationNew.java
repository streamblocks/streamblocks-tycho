package se.lth.cs.tycho.backend.c;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ctrl.Exec;
import se.lth.cs.tycho.instance.am.ctrl.Instruction;
import se.lth.cs.tycho.instance.am.ctrl.State;
import se.lth.cs.tycho.instance.am.ctrl.Test;
import se.lth.cs.tycho.instance.am.ctrl.InstructionVisitor;
import se.lth.cs.tycho.instance.am.ctrl.Wait;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

public class ScopeInitializationNew extends ScopeInitialization<State, Instruction> {
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
	protected BitSet getDependencies(Instruction t) {
		return scopeDependencies.dependencies(t);
	}

	@Override
	protected List<Instruction> getInstructions(State s) {
		return s.getInstructions();
	}

	@Override
	protected Collection<? extends State> getStates() {
		CollectStates collector = new CollectStates();
		collector.accept(actorMachine.controller().getInitialState());
		return collector.getCollectedStates();
	}

	private final class CollectStates implements Consumer<State> {
		private final LinkedHashSet<State> states;

		public CollectStates() {
			this.states = new LinkedHashSet<>();
		}

		public ImmutableList<State> getCollectedStates() {
			return states.stream().collect(ImmutableList.collector());
		}

		@Override
		public void accept(State state) {
			if (states.add(state)) {
				state.getInstructions().forEach(this::addTargets);
			}
		}

		private void addTargets(Instruction instruction) {
			instruction.forEachTarget(this);
		}
	}

	@Override
	protected BitSet killSet(Instruction i) {
		return i.accept(new InstructionVisitor<BitSet, Void>() {
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
	protected State[] targets(Instruction i) {
		return i.accept(
				exec -> new State[] { exec.target() },
				test -> new State[] { test.targetTrue(), test.targetFalse() },
				wait -> new State[] { wait.target() }
		);
	}
}
