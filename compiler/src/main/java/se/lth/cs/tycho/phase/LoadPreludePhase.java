package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;

public class LoadPreludePhase implements Phase {

	@Override
	public String getDescription() {
		return "Loads the prelude namespace.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		List<SourceUnit> sources = context.getLoader().loadNamespace(QID.of("prelude"));
		return task.withSourceUnits(ImmutableList.<SourceUnit> builder()
				.addAll(task.getSourceUnits())
				.addAll(sources)
				.build());
	}
}
