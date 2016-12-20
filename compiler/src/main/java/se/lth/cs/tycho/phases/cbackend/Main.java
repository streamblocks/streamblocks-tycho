package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.comp.Compiler;
import se.lth.cs.tycho.reporting.CompilationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Module
public interface Main {
	@Binding
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default Channels channels() {
		return backend().channels();
	}

	default MainNetwork mainNetwork() {
		return backend().mainNetwork();
	}

	default void generateCode() {
		global();
		fifo();
		main();
	}

	default void global() {
		emitter().open(target().resolve("global.h"));
		backend().global().generateGlobalHeader();
		emitter().close();
		emitter().open(target().resolve("global.c"));
		backend().global().generateGlobalCode();
		emitter().close();
	}

	default void fifo() {
		emitter().open(target().resolve("fifo.h"));
		channels().fifo_h();
		emitter().close();
	}

	default void main() {
		String targetName = backend().task().getIdentifier().getLast().toString();
		Path mainTarget = target().resolve(targetName + ".c");
		emitter().open(mainTarget);
		CompilationTask task = backend().task();
		include();
		channels().inputActorCode();
		channels().outputActorCode();
		List<GlobalEntityDecl> entityDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getEntityDecls().stream()).collect(Collectors.toList());
		backend().structure().actorDecls(entityDecls);
		mainNetwork().main(task.getNetwork());
		emitter().close();
	}

	default Path target() {
		return backend().context().getConfiguration().get(Compiler.targetPath);
	}

	default void include() {
		try (InputStream in = ClassLoader.getSystemResourceAsStream("c_backend_code/included.c")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			reader.lines().forEach(emitter()::emitRawLine);
		} catch (IOException e) {
			throw CompilationException.from(e);
		}
	}


}
