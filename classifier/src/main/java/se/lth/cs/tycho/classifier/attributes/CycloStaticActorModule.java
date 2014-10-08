package se.lth.cs.tycho.classifier.attributes;

import javarag.Cached;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.am.Condition;
import net.opendf.ir.entity.am.PredicateCondition;

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
