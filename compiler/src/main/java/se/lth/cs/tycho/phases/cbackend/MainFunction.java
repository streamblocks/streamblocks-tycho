package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Module
public interface MainFunction {
	@Binding
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default MainNetwork mainNetwork() {
		return backend().mainNetwork();
	}

	default void generateCode(CompilationTask task) {
		include();
		List<VarDecl> varDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getVarDecls().stream()).collect(Collectors.toList());
		List<EntityDecl> entityDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getEntityDecls().stream()).collect(Collectors.toList());
		backend().global().globalVariables(varDecls);
		backend().structure().actorDecls(entityDecls);
		mainNetwork().main(task.getNetwork());
	}

	default void include() {
		try (InputStream in = ClassLoader.getSystemResourceAsStream("c_backend_code/included.c")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			reader.lines().forEach(emitter()::emitRawText);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
