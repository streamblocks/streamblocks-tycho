package se.lth.cs.tycho.phases;

import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Compiler;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.phases.cbackend.Backend;
import se.lth.cs.tycho.phases.cbackend.Emitter;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class CBackendPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		String targetName = task.getIdentifier().getLast().toString();
		Path path = context.getConfiguration().get(Compiler.targetPath);
		Path mainTarget = path.resolve(targetName + ".c");
		String filename = "prelude.h";
		copyResource(context, path, filename);
		withBackend(task, context, path.resolve("fifo.h"), backend -> backend.channels().fifo_h());
		withBackend(task, context, mainTarget, backend -> backend.main().generateCode());
		return task;
	}

	private void copyResource(Context context, Path path, String filename) {
		try {
			Files.copy(ClassLoader.getSystemResourceAsStream("c_backend_code/"+filename), path.resolve(filename));
		} catch (IOException e) {
			context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not generate code to \""+filename+"\""));
		}
	}

	private void withBackend(CompilationTask task, Context context, Path target, Consumer<Backend> action) {
		try (Backend backend = openBackend(task, context, null, target)) {
			action.accept(backend);
		} catch (IOException e) {
			context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Could not generate code to \""+target+"\""));
		}
	}

	private Backend openBackend(CompilationTask task, Context context, Instance instance, Path path) throws IOException {
		return MultiJ.from(Backend.class)
				.bind("task").to(task)
				.bind("context").to(context)
				.bind("instance").to(instance)
				.bind("emitter").to(new Emitter(path))
				.instance();
	}

}
