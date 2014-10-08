package se.lth.cs.tycho.classifier.attributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javarag.Bottom;
import javarag.Circular;
import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.classifier.util.DecisionPathKnowledge;
import se.lth.cs.tycho.ir.entity.am.ICall;
import se.lth.cs.tycho.ir.entity.am.ITest;
import se.lth.cs.tycho.ir.entity.am.IWait;
import se.lth.cs.tycho.ir.entity.am.Instruction;
import se.lth.cs.tycho.ir.entity.am.State;

public class DecisionPathModule extends Module<DecisionPathModule.Decls> {

	public interface Decls {
		@Circular
		@Synthesized
		Set<DecisionPathKnowledge> decisionPaths(State s);

		@Circular
		@Synthesized
		Set<DecisionPathKnowledge> decisionPaths(Instruction i);

		State destination(IWait w);

		State destinationTrue(ITest t);

		State destinationFalse(ITest t);
	}

	@Bottom("decisionPaths")
	public Set<DecisionPathKnowledge> decisionPathsStart(State s) {
		return new HashSet<>();
	}

	@Bottom("decisionPaths")
	public Set<DecisionPathKnowledge> decisionPathsStart(Instruction i) {
		return new HashSet<>();
	}

	public Set<DecisionPathKnowledge> decisionPaths(State s) {
		Set<DecisionPathKnowledge> result = new HashSet<>();
		for (Instruction i : s.getInstructions()) {
			Set<DecisionPathKnowledge> paths = e().decisionPaths(i);
			result.addAll(paths);
		}
		return result;
	}

	public Set<DecisionPathKnowledge> decisionPaths(ICall c) {
		return Collections.singleton(new DecisionPathKnowledge(c));
	}

	public Set<DecisionPathKnowledge> decisionPaths(ITest t) {
		Set<DecisionPathKnowledge> paths = new HashSet<>();
		State stateTrue = e().destinationTrue(t);
		State stateFalse = e().destinationFalse(t);
		Set<DecisionPathKnowledge> pathsTrue = e().decisionPaths(stateTrue);
		Set<DecisionPathKnowledge> pathsFalse = e().decisionPaths(stateFalse);
		int conditionIndex = t.C();
		for (DecisionPathKnowledge p : pathsTrue) {
			paths.add(p.prepend(conditionIndex, true));
		}
		for (DecisionPathKnowledge p : pathsFalse) {
			paths.add(p.prepend(conditionIndex, false));
		}
		return paths;
	}

	public Set<DecisionPathKnowledge> decisionPaths(IWait w) {
		return e().decisionPaths(e().destination(w));
	}
}
