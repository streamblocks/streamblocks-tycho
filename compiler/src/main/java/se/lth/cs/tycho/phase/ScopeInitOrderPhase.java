package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;

public class ScopeInitOrderPhase implements Phase {
	@Override
	public String getDescription() {
		return "Orders actor machine scopes in scope initialization order.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task;
	}
}
