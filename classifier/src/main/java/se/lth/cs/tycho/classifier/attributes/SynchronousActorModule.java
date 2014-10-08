package se.lth.cs.tycho.classifier.attributes;

import java.util.Map;

import javarag.Cached;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.am.Transition;
import net.opendf.ir.entity.PortDecl;

public class SynchronousActorModule extends Module<SynchronousActorModule.Decls> {
	public interface Decls {
		@Cached
		@Synthesized
		boolean isSynchronous(ActorMachine actorMachine);

		boolean isCycloStatic(ActorMachine actorMachine);

		Map<PortDecl, Integer> outputRates(Transition t);

		Map<PortDecl, Integer> inputRates(Transition t);

	}

	public boolean isSynchronous(ActorMachine actorMachine) {
		boolean cycloStatic = e().isCycloStatic(actorMachine);
		if (!cycloStatic) {
			return false;
		}
		Map<PortDecl, Integer> inputRates = null;
		Map<PortDecl, Integer> outputRates = null;
		boolean first = true;
		for (Transition t : actorMachine.getTransitions()) {
			if (first) {
				inputRates = e().inputRates(t);
				outputRates = e().outputRates(t);
				first = false;
			} else {
				if (!inputRates.equals(e().inputRates(t))) {
					return false;
				}
				if (!outputRates.equals(e().outputRates(t))) {
					return false;
				}
			}
		}
		return true;
	}
}
