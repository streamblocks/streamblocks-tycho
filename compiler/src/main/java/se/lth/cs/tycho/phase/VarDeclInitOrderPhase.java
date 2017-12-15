package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;

public class VarDeclInitOrderPhase implements Phase {
	@Override
	public String getDescription() {
		return "Orders variable declarations in initialization order.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task;
	}
}
