package se.lth.cs.tycho.phases;

import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.Namespaces;
import se.lth.cs.tycho.phases.attributes.ActorMachineScopes;
import se.lth.cs.tycho.phases.attributes.AttributeManager;
import se.lth.cs.tycho.phases.attributes.GlobalNames;
import se.lth.cs.tycho.phases.attributes.Names;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.phases.cbackend.Backend;
import se.lth.cs.tycho.comp.Compiler;
import se.lth.cs.tycho.phases.cbackend.Emitter;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class CBackendPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		String targetName = Namespaces.findEntities(task, task.getIdentifier())
				.findFirst().get().getOriginalName();

		Path path = context.getConfiguration().get(Compiler.targetPath);
		Path target = path.resolve(targetName + ".c");
		PrintWriter writer;
		try {
			writer = new PrintWriter(Files.newBufferedWriter(target));
		} catch (IOException e) {
			context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR, e.getMessage()));
			return task;
		}
		Backend backend = MultiJ.from(Backend.class)
				.bind("task").to(task)
				.bind("context").to(context)
				.instance();
		backend.main().generateCode(task);
		writer.close();
		return task;
	}

}
