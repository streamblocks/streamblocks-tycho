package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.comp.SyntheticSourceUnit;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RemoveUnusedEntityDeclsPhase implements Phase {
	@Override
	public String getDescription() {
		return "Removes entity declarations that are note directly used in the network.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Set<QID> entities = new HashSet<>();
		entities.add(task.getIdentifier());
		task.getNetwork().getInstances().stream()
				.map(Instance::getEntityName)
				.forEach(entities::add);
		List<VarDecl> varDecls = getAll(task, NamespaceDecl::getVarDecls).collect(Collectors.toList());
		List<TypeDecl> typeDecls = getAll(task, NamespaceDecl::getTypeDecls).collect(Collectors.toList());
		List<EntityDecl> entityDecls = task.getSourceUnits().stream()
				.flatMap(unit -> {
					QID ns = unit.getTree().getQID();
					return unit.getTree().getEntityDecls().stream()
							.filter(entity -> {
								QID name = ns.concat(QID.of(entity.getName()));
								return entities.contains(name);
							});
				}).collect(Collectors.toList());
		List<Import> imports = getAll(task, NamespaceDecl::getImports).collect(Collectors.toList());
		SourceUnit unit = new SyntheticSourceUnit(new NamespaceDecl(QID.empty(), imports, varDecls, entityDecls, typeDecls));
		return task.withSourceUnits(ImmutableList.of(unit));
	}

	private <T> Stream<T> getAll(CompilationTask task, Function<NamespaceDecl, Collection<T>> get) {
		return task.getSourceUnits().stream().map(SourceUnit::getTree).map(get).flatMap(Collection::stream);
	}
}
