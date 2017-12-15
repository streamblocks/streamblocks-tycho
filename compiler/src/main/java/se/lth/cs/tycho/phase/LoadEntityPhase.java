package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.util.List;

public class LoadEntityPhase implements Phase {

	@Override
	public String getDescription() {
		return "Loads the namespace of the entity that is compiled.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		QID namespace = task.getIdentifier().getButLast();
		String entityName = task.getIdentifier().getLast().toString();
		List<SourceUnit> sources = context.getLoader()
				.loadNamespace(namespace);
		long count = sources.stream()
				.map(SourceUnit::getTree)
				.flatMap(ns -> ns.getEntityDecls().stream())
				.filter(entityDecl -> entityDecl.getName().equals(entityName))
				.filter(entityDecl -> entityDecl.getAvailability() == Availability.PUBLIC)
				.count();
		if (count == 0) {
			context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR,
					"Could not find public entity \"" + task.getIdentifier() + "\"."));
			return task;
		} else {
			return task.withSourceUnits(sources);
		}
	}
}
