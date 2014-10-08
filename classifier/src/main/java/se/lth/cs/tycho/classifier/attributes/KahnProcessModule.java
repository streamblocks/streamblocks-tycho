package se.lth.cs.tycho.classifier.attributes;

import java.util.Map;
import java.util.Set;

import javarag.Cached;
import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.classifier.util.DecisionPathKnowledge;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.ICall;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.State;
import se.lth.cs.tycho.ir.entity.am.Transition;

public class KahnProcessModule extends Module<KahnProcessModule.Decls> {
	public interface Decls {
		@Cached
		@Synthesized
		boolean isKahnProcess(ActorMachine am);

		@Synthesized
		boolean isKahnProcess(State s);

		boolean isMonotonic(ActorMachine am);

		Set<DecisionPathKnowledge> decisionPaths(State s);

		Transition transition(ICall destination);

		Map<PortDecl, Integer> inputRates(Transition t);

		Map<PortDecl, Integer> outputRates(Transition t);

		Condition lookupCondition(IRNode n, int c);

		PortDecl declaration(Port portName);
	}

	public boolean isKahnProcess(ActorMachine am) {
		if (e().isMonotonic(am)) {
			for (State s : am.getController()) {
				boolean kahn = e().isKahnProcess(s);
				if (!kahn) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean isKahnProcess(State s) {
		Set<DecisionPathKnowledge> paths = e().decisionPaths(s);
		for (DecisionPathKnowledge path : paths) {
			Transition t = e().transition(path.getDestination());
			Map<PortDecl, Integer> inputRates = e().inputRates(t);
			// Map<PortDecl, Integer> outputRates = e().outputRates(t);
			for (int c : path.getTrueConditions()) {
				Condition cond = e().lookupCondition(s, c);
				if (cond instanceof PortCondition) {
					PortCondition p = (PortCondition) cond;
					PortDecl port = e().declaration(p.getPortName());
					int transitionInputRate = inputRates.containsKey(port) ? inputRates.get(port) : 0;
					if (transitionInputRate < p.N()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
