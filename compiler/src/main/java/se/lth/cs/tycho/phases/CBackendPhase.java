package se.lth.cs.tycho.phases;

import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.phases.attributes.ActorMachineScopes;
import se.lth.cs.tycho.phases.attributes.AttributeManager;
import se.lth.cs.tycho.phases.attributes.Names;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.phases.cbackend.Backend;
import se.lth.cs.tycho.comp.Compiler;
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
		String targetName = task.getTarget().getEntityDecls().stream()
				.filter(decl -> decl.getName().equals(task.getIdentifier().toString()))
				.findFirst()
				.get()
				.getOriginalName();

		Path path = context.getConfiguration().get(Compiler.targetPath);
		Path target = path.resolve(targetName + ".c");
		PrintWriter writer;
		try {
			writer = new PrintWriter(Files.newBufferedWriter(target));
		} catch (IOException e) {
			context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR, e.getMessage()));
			return task;
		}
		AttributeManager manager = context.getAttributeManager();
		Backend backend = MultiJ.from(Backend.class)
				.bind("types").to(manager.getAttributeModule(Types.key, task))
				.bind("names").to(manager.getAttributeModule(Names.key, task))
				.bind("uniqueNumbers").to(context.getUniqueNumbers())
				.bind("tree").to(manager.getAttributeModule(TreeShadow.key, task))
				.bind("scopes").to(manager.getAttributeModule(ActorMachineScopes.key, task))
				.bind("emitter.writer").to(writer)
				.instance();
		backend.main().generateCode(task);
		writer.close();
		return task;
	}

}
