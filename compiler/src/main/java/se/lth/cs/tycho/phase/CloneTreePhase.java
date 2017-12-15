package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;

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
