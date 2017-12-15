package se.lth.cs.tycho.phase;

import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Compiler;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.backend.c.Backend;
import se.lth.cs.tycho.backend.c.Controllers;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.settings.Setting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class CBackendPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return ImmutableList.of(Controllers.scopeLivenessAnalysis);
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Path path = context.getConfiguration().get(Compiler.targetPath);
		String filename = "prelude.h";
		copyResource(path, filename);
		Backend backend = MultiJ.from(Backend.class)
				.bind("task").to(task)
				.bind("context").to(context)
				.instance();
		backend.main().generateCode();
		return task;
	}

	private void copyResource(Path path, String filename) {
		try {
			Files.copy(ClassLoader.getSystemResourceAsStream("c_backend_code/"+filename), path.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Could not generate code to \""+filename+"\""));
		}
	}


}
