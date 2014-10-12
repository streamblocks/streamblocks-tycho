package se.lth.cs.tycho.classifier.attributes;

import java.util.BitSet;
import java.util.Set;

import javarag.Cached;
import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.classifier.util.DecisionPathKnowledge;
import se.lth.cs.tycho.classifier.util.ImmutableBitSet;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.instance.am.State;

public class MonotonicityModule extends Module<MonotonicityModule.Decls> {
	public interface Decls {
		@Cached
		@Synthesized
		boolean isMonotonic(ActorMachine actorMachine);

		@Synthesized
		ImmutableBitSet portConditions(ActorMachine actorMachine);

		@Synthesized
		public ImmutableBitSet predicateConditions(ActorMachine actorMachine);

		boolean isDeterministic(ActorMachine actorMachine);

		Set<DecisionPathKnowledge> decisionPaths(State s);
	}

	public boolean isMonotonic(ActorMachine actorMachine) {
		if (e().isDeterministic(actorMachine)) {
			ImmutableBitSet portConds = e().portConditions(actorMachine);
			for (State s : actorMachine.getController()) {
				Set<DecisionPathKnowledge> paths = e().decisionPaths(s);
				for (DecisionPathKnowledge path : paths) {
					if (path.getFalseConditions().intersects(portConds)) {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public ImmutableBitSet portConditions(ActorMachine actorMachine) {
		BitSet portConds = new BitSet();
		int i = 0;
		for (Condition c : actorMachine.getConditions()) {
			if (c instanceof PortCondition) {
				portConds.set(i);
			}
			i += 1;
		}
		return ImmutableBitSet.fromBitSet(portConds);
	}

	public ImmutableBitSet predicateConditions(ActorMachine actorMachine) {
		BitSet predConds = new BitSet();
		int i = 0;
		for (Condition c : actorMachine.getConditions()) {
			if (c instanceof PredicateCondition) {
				predConds.set(i);
			}
			i += 1;
		}
		return ImmutableBitSet.fromBitSet(predConds);
	}

}
