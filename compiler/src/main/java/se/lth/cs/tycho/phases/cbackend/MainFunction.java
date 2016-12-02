package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Compiler;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.types.BoolType;
import se.lth.cs.tycho.types.IntType;
import se.lth.cs.tycho.types.RealType;
import se.lth.cs.tycho.types.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

@Module
public interface MainFunction {
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
		CompilationTask task = backend().task();
		String targetName = task.getIdentifier().getLast().toString();
		Path path = backend().context().getConfiguration().get(Compiler.targetPath);
		Path target = path.resolve(targetName + ".c");
		emitter().open(target);
		include();
		for (Type t : channelTypes()) { channels().channelCode(t); }
		for (Type t : inputActorTypes()) { channels().inputActorCode(t); }
		for (Type t : outputActorTypes()) { channels().outputActorCode(t); }
		backend().callables().declareCallables();
		List<VarDecl> varDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getVarDecls().stream()).collect(Collectors.toList());
		List<GlobalEntityDecl> entityDecls = task.getSourceUnits().stream().flatMap(unit -> unit.getTree().getEntityDecls().stream()).collect(Collectors.toList());
		backend().callables().declareEnvironmentForCallablesInScope(task);
		backend().global().globalVariables(varDecls);
		backend().callables().defineCallables();
		backend().structure().actorDecls(entityDecls);
		mainNetwork().main(task.getNetwork());
		emitter().close();
	}

	default Type intToNearest8Mult(Type t) {
		return t;
	}

	default IntType intToNearest8Mult(IntType t) {
		if (t.getSize().isPresent()) {
			int size = t.getSize().getAsInt();
			int limit = 8;
			while (size > limit) {
				limit = limit + limit;
			}
			return new IntType(OptionalInt.of(limit), t.isSigned());
		} else {
			return new IntType(OptionalInt.of(32), t.isSigned());
		}
	}

	default List<Type> channelTypes() {
		Network network = backend().task().getNetwork();
		return backend().task().getNetwork().getConnections().stream()
				.map(connection -> backend().types().connectionType(network, connection))
				.map(this::intToNearest8Mult)
				.distinct()
				.collect(Collectors.toList());
	}

	default List<Type> outputActorTypes() {
		Network network = backend().task().getNetwork();
		return network.getOutputPorts().stream()
				.map(backend().types()::declaredPortType)
				.distinct()
				.collect(Collectors.toList());
	}

	default List<Type> inputActorTypes() {
		Network network = backend().task().getNetwork();
		return network.getInputPorts().stream()
				.map(backend().types()::declaredPortType)
				.distinct()
				.collect(Collectors.toList());
	}

	default void include() {
		try (InputStream in = ClassLoader.getSystemResourceAsStream("c_backend_code/included.c")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			reader.lines().forEach(emitter()::emitRawLine);
		} catch (IOException e) {
			throw new Error(e);
		}
	}


}
