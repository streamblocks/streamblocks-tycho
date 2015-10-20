package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.comp.CompilationTask;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Module
public interface Main {
	@Binding
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default void generateCode(CompilationTask task) {
		include();
		task.getTarget().getEntityDecls().forEach(backend().structure()::entityDecl);
		main(task);
	}

	default void include() {
		try {
			Path path = Paths.get(ClassLoader.getSystemResource("c_backend_code/included.c").toURI());
			Files.readAllLines(path).forEach(emitter()::emitRawText);
		} catch (URISyntaxException | IOException e) {
			throw new Error(e);
		}
	}

	default void main(CompilationTask task) {
		emitter().emit("int main(int argc, char **argv) {");
		emitter().increaseIndentation();
		emitter().emit("register_SIGINT_handler();");
		emitter().emit("return %s_main(argc, argv);", task.getIdentifier());
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
		emitter().emit("");
	}
}
