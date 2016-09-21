package se.lth.cs.tycho.phases;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

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

			sourceUnit.walk()
					.flatMap(nsRefs::get)
					.distinct()
					.filter(ns -> !loaded.contains(ns))
					.forEach(ns -> {
						loaded.add(ns);
						List<SourceUnit> unit = context.getLoader().loadNamespace(ns);
						result.addAll(unit);
						queue.addAll(unit);
					});
		}
		return task.withSourceUnits(result);
	}

	private static final NamespaceReferences nsRefs = MultiJ.instance(NamespaceReferences.class);

	@Module
	interface NamespaceReferences {
		default Stream<QID> get(IRNode node) {
			return Stream.empty();
		}

		default Stream<QID> get(SingleImport imp) {
			return Stream.of(imp.getGlobalName().getButLast());
		}

		default Stream<QID> get(GroupImport imp) {
			return Stream.of(imp.getGlobalName());
		}

		default Stream<QID> get(ExprGlobalVariable variable) {
			return Stream.of(variable.getGlobalName().getButLast());
		}

		default Stream<QID> get(EntityReferenceGlobal entity) {
			return Stream.of(entity.getGlobalName().getButLast());
		}
	}
}
