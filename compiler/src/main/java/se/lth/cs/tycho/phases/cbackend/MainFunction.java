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

	default void generateCode(CompilationTask task) {
		include();
		List<VarDecl> varDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getVarDecls().stream()).collect(Collectors.toList());
		List<EntityDecl> entityDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getEntityDecls().stream()).collect(Collectors.toList());
		backend().global().globalVariables(varDecls);
		backend().structure().actorDecls(entityDecls);
		task.getIdentifier();
		for (EntityDecl decl : entityDecls) {
			if (decl.getName().equals(task.getIdentifier().toString())) {
				backend().structure().networkDecl(decl);
			}
		}
		main(task);
	}

	default void include() {
		try (InputStream in = ClassLoader.getSystemResourceAsStream("c_backend_code/included.c")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			reader.lines().forEach(emitter()::emitRawText);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	default void main(CompilationTask task) {
		emitter().emit("int main(int argc, char **argv) {");
		emitter().increaseIndentation();
		emitter().emit("register_SIGINT_handler();");
		emitter().emit("init_global_variables();");
		emitter().emit("return %s_main(argc, argv);", task.getIdentifier());
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
		emitter().emit("");
	}
}
