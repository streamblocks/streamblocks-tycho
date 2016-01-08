package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.StarImport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class LoadImportsPhase implements Phase {
	@Override
	public String getDescription() {
		return "Loads the namespaces that import declarations refer to.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		List<SourceUnit> result = new ArrayList<>(task.getSourceUnits());
		Set<QID> loaded = new HashSet<>();
		loaded.add(task.getIdentifier().getButLast());
		Queue<SourceUnit> queue = new ArrayDeque<>(task.getSourceUnits());
		while (!queue.isEmpty()) {
			SourceUnit sourceUnit = queue.remove();
			for (Decl decl : sourceUnit.getTree().getAllDecls()) {
				if (decl.isImport()) {
					QID namespace = decl.getQualifiedIdentifier().getButLast();
					if (loaded.add(namespace)) {
						List<SourceUnit> ns = context.getLoader().loadNamespace(namespace);
						result.addAll(ns);
						queue.addAll(ns);
					}
				}
			}
			for (StarImport starImport : sourceUnit.getTree().getStarImports()) {
				if (loaded.add(starImport.getQID())) {
					List<SourceUnit> ns = context.getLoader().loadNamespace(starImport.getQID());
					result.addAll(ns);
					queue.addAll(ns);
				}
			}
		}
		return task.withSourceUnits(result);
	}
}
