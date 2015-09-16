package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.StarImport;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpandStarImportsPhase implements Phase {

	@Override
	public String getDescription() {
		return "Expands \"import a.*;\" to \"import a.b;import a.c;\", for every variable declaration in a.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Task t = new Task(task, context);
		return t.execute();
	}

	private static class Task {
		private final CompilationTask task;
		private final Context context;
		private Map<QID, Set<String>> starImportedNames;

		public Task(CompilationTask task, Context context) {
			this.task = task;
			this.context = context;
			this.starImportedNames = new HashMap<>();
			init();
		}

		private NamespaceDecl transform(NamespaceDecl nsDecl) {
			if (nsDecl.getStarImports().isEmpty()) {
				return nsDecl;
			} else {
				ImmutableList.Builder<VarDecl> builder = ImmutableList.builder();
				builder.addAll(nsDecl.getVarDecls());
				for (StarImport starImport : nsDecl.getStarImports()) {
					for (String name : starImportedNames.get(starImport.getQID())) {
						VarDecl decl = VarDecl.importDecl(starImport.getQID().concat(QID.of(name)), name);
						builder.add(decl);
					}
				}
				return new NamespaceDecl(nsDecl.getQID(), ImmutableList.empty(), builder.build(), nsDecl.getEntityDecls(), nsDecl.getTypeDecls());
			}
		}

		private void checkCollisions(SourceUnit unit) {
			Map<String, List<StarImport>> importedFrom = new HashMap<>();
			for (StarImport starImport : unit.getTree().getStarImports()) {
				for (String name : starImportedNames.get(starImport.getQID())) {
					importedFrom.computeIfAbsent(name, n -> new ArrayList<>())
							.add(starImport);
				}
			}
			for (Map.Entry<String, List<StarImport>> entry : importedFrom.entrySet()) {
				if (entry.getValue().size() > 1) {
					context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR,
							"The name \"" + entry.getKey() + "\" is imported from several namespaces:\n\t" +
									entry.getValue().stream()
											.map(StarImport::getQID)
											.map(QID::toString)
											.collect(Collectors.joining("\n\t")),
							unit));
				}
			}
		}

		public CompilationTask execute() {
			task.getSourceUnits().forEach(this::checkCollisions);
			return task.withSourceUnits(
					task.getSourceUnits().stream()
							.map(unit -> unit.withTree(transform(unit.getTree())))
							.collect(Collectors.toList()));
		}

		private void init() {
			for (SourceUnit unit : task.getSourceUnits()) {
				for (StarImport starImport : unit.getTree().getStarImports()) {
					QID qid = starImport.getQID();
					if (!starImportedNames.containsKey(qid)) {
						starImportedNames.put(qid, new LinkedHashSet<>());
					}
				}
			}
			for (SourceUnit unit : task.getSourceUnits()) {
				QID qid = unit.getTree().getQID();
				if (starImportedNames.containsKey(qid)) {
					Set<String> names = starImportedNames.get(qid);
					unit.getTree().getVarDecls().stream()
							.filter(varDecl -> varDecl.getAvailability() == Availability.PUBLIC)
							.map(VarDecl::getName)
							.forEach(names::add);
				}
			}
		}

	}
}
