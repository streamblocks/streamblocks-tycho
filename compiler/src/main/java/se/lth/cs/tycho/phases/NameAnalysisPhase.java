package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.StarImport;
import se.lth.cs.tycho.phases.attributes.NameAnalysis;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NameAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Analyzes name binding.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		NameAnalysis nameAnalysis = context.getAttributeManager().getAttributeModule(NameAnalysis.key, task);

		nameAnalysis.checkNames(task, null, context.getReporter());

		for (SourceUnit sourceUnit : task.getSourceUnits()) {
			NamespaceDecl ns = sourceUnit.getTree();
			Map<String, List<StarImport>> imported = new HashMap<>();
			for (StarImport i : ns.getStarImports()) {
				for (String name : nameAnalysis.starImported().getOrDefault(i, Collections.emptySet())) {
					imported.computeIfAbsent(name, n -> new ArrayList<>()).add(i);
				}
			}
			imported.entrySet().stream()
					.filter(entry -> entry.getValue().size() > 1)
					.forEach(entry -> context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR,
							"Variable " + entry.getKey() + " is imported with several import statements:\n" +
									entry.getValue().stream()
											.map(starImport -> "\timport " + starImport.getQID() + ".*;\n")
											.collect(Collectors.joining()))));
		}
		return task;
	}

}
