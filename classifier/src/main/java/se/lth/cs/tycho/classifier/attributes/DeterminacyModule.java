package se.lth.cs.tycho.classifier.attributes;

import java.util.Set;

import javarag.Cached;
import javarag.Collected;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.Builders;
import javarag.coll.Collector;
import se.lth.cs.tycho.classifier.util.DecisionPathKnowledge;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.ICall;
import se.lth.cs.tycho.ir.entity.am.State;

public class DeterminacyModule extends Module<DeterminacyModule.Decls> {
	public interface Decls {
		@Cached
		@Synthesized
		boolean isDeterministic(ActorMachine actorMachine);

		@Synthesized
		boolean isDeterministic(State s);

		Set<DecisionPathKnowledge> decisionPaths(State s);

		@Inherited
		ActorMachine actorMachine(IRNode n);
		
		@Collected
		Set<State> decisionStartStates(ActorMachine am);

		State destination(ICall c);

	}

	public boolean isDeterministic(ActorMachine actorMachine) {
		for (State s : e().decisionStartStates(actorMachine)) {
			if (!e().isDeterministic(s)) {
				return false;
			}
		}
		return true;
	}

	public boolean isDeterministic(State s) {
		Set<DecisionPathKnowledge> paths = e().decisionPaths(s);
		for (DecisionPathKnowledge pathA : paths) {
			for (DecisionPathKnowledge pathB : paths) {
				if (pathA.getDestination().T() != pathB.getDestination().T()) {
					boolean trueInAFalseInB = pathA.getTrueConditions().intersects(pathB.getFalseConditions());
					boolean falseInATrueInB = pathA.getFalseConditions().intersects(pathB.getTrueConditions());
					boolean differentResult = trueInAFalseInB || falseInATrueInB;
					if (!differentResult) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public Builder<Set<State>, State> decisionStartStates(ActorMachine am) {
		return Builders.setBuilder();
	}
	
	public void decisionStartStates(ICall c, Collector<State> coll) {
		coll.add(e().actorMachine(c), e().destination(c));
	}
	
	public void decisionStartStates(ActorMachine am, Collector<State> coll) {
		coll.add(am, am.getController().get(0));
	}
	
	public ActorMachine actorMachine(ActorMachine am) {
		return am;
	}

}
