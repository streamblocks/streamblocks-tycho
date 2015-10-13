package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;

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
