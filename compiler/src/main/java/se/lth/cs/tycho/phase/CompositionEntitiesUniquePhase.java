package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.transformation.DuplicateEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositionEntitiesUniquePhase implements Phase {
	@Override
	public String getDescription() {
		return "Makes entities part of compositions uniquely named.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		if (context.getConfiguration().get(CompositionPhase.actorComposition)) {
			List<String> instances = task.getNetwork().getConnections().stream()
					.filter(connection -> connection.getValueAttribute("composition").isPresent())
					.flatMap(connection -> Stream.of(connection.getSource().getInstance(), connection.getTarget().getInstance()))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.distinct()
					.collect(Collectors.toList());
			for (String instance : instances) {
				task = DuplicateEntity.duplicateEntity(task, instance, context.getUniqueNumbers());
			}
			return task.deepClone();
		} else {
			return task;
		}
	}
}
