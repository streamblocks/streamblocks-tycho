package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationUnit;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
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
	public CompilationUnit execute(CompilationUnit unit, Context context) {
		QID namespace = unit.getIdentifier().getButLast();
		String entityName = unit.getIdentifier().getLast().toString();
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
					"Could not find public entity \"" + unit.getIdentifier() + "\"."));
			return unit;
		} else {
			return unit.withSourceUnits(sources);
		}
	}
}
