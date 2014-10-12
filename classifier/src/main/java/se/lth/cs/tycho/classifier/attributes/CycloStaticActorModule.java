package se.lth.cs.tycho.classifier.attributes;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import javarag.Cached;
import javarag.Module;
import javarag.Synthesized;

public class CycloStaticActorModule extends Module<CycloStaticActorModule.Decls> {
	public interface Decls {
		@Cached
		@Synthesized
		public boolean isCycloStatic(ActorMachine actorMachine);

		public boolean isKahnProcess(ActorMachine actorMachine);
	}

	public boolean isCycloStatic(ActorMachine actorMachine) {
		boolean kahn = e().isKahnProcess(actorMachine);
		if (!kahn) {
			return false;
		}
		for (Condition cond : actorMachine.getConditions()) {
			if (cond instanceof PredicateCondition) {
				return false;
			}
		}
		return true;
	}

}
