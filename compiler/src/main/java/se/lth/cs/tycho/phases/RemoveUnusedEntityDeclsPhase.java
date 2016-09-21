package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.network.Instance;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
		return task.withSourceUnits(task.getSourceUnits().map(unit ->
				unit.withTree(unit.getTree().withEntityDecls(unit.getTree().getEntityDecls().stream().filter(decl ->
						entities.contains(unit.getTree().getQID().concat(QID.of(decl.getName())))).collect(Collectors.toList())))));
	}

}
