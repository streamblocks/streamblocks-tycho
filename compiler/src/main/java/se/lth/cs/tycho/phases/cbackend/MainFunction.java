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
		for (Type t : channelTypes()) { channelCode(t); }
		for (Type t : inputActorTypes()) { inputActorCode(t); }
		for (Type t : outputActorTypes()) { outputActorCode(t); }
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

	default void ioCode(Type type) {
		channelCode(type);
		inputActorCode(type);
		outputActorCode(type);
	}

	default void channelCode(Type type) {
		String tokenType = backend().code().type(type);
		emitter().emit("// CHANNEL %s", type);
		emitter().emit("typedef struct {");
		emitter().emit("	size_t head;");
		emitter().emit("	size_t tokens;");
		emitter().emit("	%s *buffer;", tokenType);
		emitter().emit("} channel_%s;", tokenType);
		emitter().emit("");

		emitter().emit("static inline _Bool channel_has_data_%s(channel_%1$s *channel, size_t tokens) {", tokenType);
		emitter().emit("	return channel->tokens >= tokens;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline _Bool channel_has_space_%s(channel_%1$s *channel_vector[], size_t channel_count, size_t tokens) {", tokenType);
		emitter().emit("	for (size_t i = 0; i < channel_count; i++) {");
		emitter().emit("		if (BUFFER_SIZE - channel_vector[i]->tokens < tokens) {");
		emitter().emit("			return false;");
		emitter().emit("		}");
		emitter().emit("	}");
		emitter().emit("	return true;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline void channel_write_one_%s(channel_%1$s *channel_vector[], size_t channel_count, %1$s data) {", tokenType);
		emitter().emit("	for (size_t c = 0; c < channel_count; c++) {");
		emitter().emit("		channel_%s *chan = channel_vector[c];", tokenType);
		emitter().emit("		chan->buffer[(chan->head + chan->tokens) %% BUFFER_SIZE] = data;");
		emitter().emit("		chan->tokens++;");
		emitter().emit("	}");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline void channel_write_%s(channel_%1$s *channel_vector[], size_t channel_count, %1$s *data, size_t tokens) {", tokenType);
		emitter().emit("	for (size_t c = 0; c < channel_count; c++) {");
		emitter().emit("		channel_%s *chan = channel_vector[c];", tokenType);
		emitter().emit("		for (size_t i = 0; i < tokens; i++) {");
		emitter().emit("			chan->buffer[(chan->head + chan->tokens) %% BUFFER_SIZE] = data[i];");
		emitter().emit("			chan->tokens++;");
		emitter().emit("		}");
		emitter().emit("	}");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline %s channel_peek_first_%1$s(channel_%1$s *channel) {", tokenType);
		emitter().emit("	return channel->buffer[channel->head];");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline void channel_peek_%s(channel_%1$s *channel, size_t offset, size_t tokens, %1$s *result) {", tokenType);
		emitter().emit("	%s *res = result;", tokenType);
		emitter().emit("	for (size_t i = 0; i < tokens; i++) {");
		emitter().emit("		res[i] = channel->buffer[(channel->head+i+offset) %% BUFFER_SIZE];");
		emitter().emit("	}");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline void channel_consume_%s(channel_%1$s *channel, size_t tokens) {", tokenType);
		emitter().emit("	channel->tokens -= tokens;");
		emitter().emit("	channel->head = (channel->head + tokens) %% BUFFER_SIZE;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static channel_%s *channel_create_%1$s() {", tokenType);
		emitter().emit("	%s *buffer = malloc(sizeof(%1$s)*BUFFER_SIZE);", tokenType);
		emitter().emit("	channel_%s *channel = malloc(sizeof(channel_%1$s));", tokenType);
		emitter().emit("	channel->head = 0;");
		emitter().emit("	channel->tokens = 0;");
		emitter().emit("	channel->buffer = buffer;");
		emitter().emit("	return channel;");
		emitter().emit("}");
		emitter().emit("");


		emitter().emit("static void channel_destroy_%s(channel_%1$s *channel) {", tokenType);
		emitter().emit("	free(channel->buffer);");
		emitter().emit("	free(channel);");
		emitter().emit("}");
		emitter().emit("");
	}

	default void inputActorCode(Type type) {
		String tokenType = backend().code().type(type);

		emitter().emit("typedef struct {");
		emitter().emit("	size_t channelc;");
		emitter().emit("	channel_%s **channelv;", tokenType);
		emitter().emit("	FILE *stream;");
		emitter().emit("} input_actor_%s;", tokenType);
		emitter().emit("");

		emitter().emit("static input_actor_%s *input_actor_create_%1$s(FILE *stream, channel_%1$s *channel_vector[], size_t channel_count) {", tokenType);
		emitter().emit("    input_actor_%s *actor = malloc(sizeof(input_actor_%1$s));", tokenType);
		emitter().emit("    actor->channelv = channel_vector;");
		emitter().emit("    actor->channelc = channel_count;");
		emitter().emit("    actor->stream = stream;");
		emitter().emit("    return actor;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static void input_actor_destroy_%s(input_actor_%1$s *actor) {", tokenType);
		emitter().emit("    free(actor);");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static _Bool input_actor_run_%s(input_actor_%1$s *actor) {", tokenType);
		emitter().emit("    size_t tokens = SIZE_MAX;");
		emitter().emit("    for (size_t i = 0; i < actor->channelc; i++) {");
		emitter().emit("        size_t s = BUFFER_SIZE - actor->channelv[i]->tokens;");
		emitter().emit("        tokens = s < tokens ? s : tokens;");
		emitter().emit("    }");
		emitter().emit("    if (tokens > 0) {");
		emitter().emit("        %s buf[tokens];", tokenType);
		emitter().emit("        tokens = fread(buf, sizeof(%s), tokens, actor->stream);", tokenType);
		emitter().emit("        if (tokens > 0) {");
		emitter().emit("            channel_write_%s(actor->channelv, actor->channelc, buf, tokens);", tokenType);
		emitter().emit("            return true;");
		emitter().emit("        } else {");
		emitter().emit("            return false;");
		emitter().emit("        }");
		emitter().emit("    } else {");
		emitter().emit("        return false;");
		emitter().emit("    }");
		emitter().emit("}");
		emitter().emit("");
	}

	default void outputActorCode(Type type) {
		String tokenType = backend().code().type(type);

		emitter().emit("typedef struct {");
		emitter().emit("	channel_%s *channel;", tokenType);
		emitter().emit("	FILE *stream;");
		emitter().emit("} output_actor_%s;", tokenType);
		emitter().emit("");

		emitter().emit("static output_actor_%s *output_actor_create_%1$s(FILE *stream, channel_%1$s *channel) {", tokenType);
		emitter().emit("    output_actor_%s *actor = malloc(sizeof(output_actor_%1$s));", tokenType);
		emitter().emit("    actor->channel = channel;");
		emitter().emit("    actor->stream = stream;");
		emitter().emit("    return actor;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static void output_actor_destroy_%s(output_actor_%1$s *actor) {", tokenType);
		emitter().emit("    free(actor);");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static _Bool output_actor_run_%s(output_actor_%1$s* actor) {", tokenType);
		emitter().emit("    channel_%s *channel = actor->channel;", tokenType);
		emitter().emit("    if (channel->tokens > 0) {");
		emitter().emit("        size_t wrap_or_end = channel->head + channel->tokens;");
		emitter().emit("        if (wrap_or_end > BUFFER_SIZE) {");
		emitter().emit("            wrap_or_end = BUFFER_SIZE;");
		emitter().emit("        }");
		emitter().emit("        size_t tokens_before_wrap = wrap_or_end - channel->head;");
		emitter().emit("        fwrite(&channel->buffer[channel->head], sizeof(%s), tokens_before_wrap, actor->stream);", tokenType);
		emitter().emit("");
		emitter().emit("        size_t tokens_after_wrap = channel->tokens - tokens_before_wrap;");
		emitter().emit("        if (tokens_after_wrap > 0) {");
		emitter().emit("            fwrite(&channel->buffer, sizeof(%s), tokens_after_wrap, actor->stream);", tokenType);
		emitter().emit("        }");
		emitter().emit("");
		emitter().emit("        //channel->head = (channel->head + channel->tokens) %% BUFFER_SIZE;");
		emitter().emit("        channel->tokens = 0;");
		emitter().emit("        return true;");
		emitter().emit("    } else {");
		emitter().emit("        return false;");
		emitter().emit("    }");
		emitter().emit("}");
		emitter().emit("");
	}

}
