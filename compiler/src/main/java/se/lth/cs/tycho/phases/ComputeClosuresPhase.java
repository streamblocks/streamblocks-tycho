package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.transformation.ComputeClosures;

public class ComputeClosuresPhase implements Phase {
	@Override
	public String getDescription() {
		return "Creates closures for functions and procedures.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return (CompilationTask) ComputeClosures.computeClosures(task, context.getUniqueNumbers());
	}
}
