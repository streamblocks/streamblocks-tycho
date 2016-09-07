package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;

public class CloneTreePhase implements Phase {
	@Override
	public String getDescription() {
		return "Clones the tree such no nodes are shared.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.deepClone();
	}
}
