package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.comp.CompilationTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Module
public interface MainFunction {
	@Binding
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default void generateCode(CompilationTask task) {
		include();
		backend().global().globalVariables(task.getTarget().getVarDecls());
		backend().structure().entityDecls(task.getTarget().getEntityDecls());
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
