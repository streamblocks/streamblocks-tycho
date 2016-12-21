package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.types.Type;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Module
public interface AlternativeChannels extends Channels {
	@Binding
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default void channelListCodeForType(Type type, int size) {
		String tokenType = backend().code().type(type);
		String sizeString = sizeToString(size);

		emitter().emit("");
		emitter().emit("static inline _Bool channel_has_space_%s_%s(channel_%1$s_%2$s *channel_vector[], size_t channel_count, size_t tokens) {", tokenType, sizeString);
		emitter().emit("	for (size_t i = 0; i < channel_count; i++) {");
		emitter().emit("		if (BUFFER_SIZE - (channel_vector[i]->write - channel_vector[i]->read) < tokens) {");
		emitter().emit("			return false;");
		emitter().emit("		}");
		emitter().emit("	}");
		emitter().emit("	return true;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline void channel_write_one_%s_%s(channel_%1$s_%2$s *channel_vector[], size_t channel_count, %1$s data) {", tokenType, sizeString);
		emitter().emit("	for (size_t c = 0; c < channel_count; c++) {");
		emitter().emit("		channel_%s_%s *chan = channel_vector[c];", tokenType, sizeString);
		emitter().emit("		chan->buffer[chan->write %% BUFFER_SIZE] = data;");
		emitter().emit("		chan->write++;");
		emitter().emit("	}");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline void channel_write_%s_%s(channel_%1$s_%2$s *channel_vector[], size_t channel_count, %1$s *data, size_t tokens) {", tokenType, sizeString);
		emitter().emit("	for (size_t c = 0; c < channel_count; c++) {");
		emitter().emit("		channel_%s_%s *chan = channel_vector[c];", tokenType, sizeString);
		emitter().emit("		for (size_t i = 0; i < tokens; i++) {");
		emitter().emit("			chan->buffer[chan->write %% BUFFER_SIZE] = data[i];");
		emitter().emit("			chan->write++;");
		emitter().emit("		}");
		emitter().emit("	}");
		emitter().emit("}");
		emitter().emit("");

	}

	default String sourceEndTypeSize(Connection.End source) {
		Network network = backend().task().getNetwork();
		List<Connection> connections = network.getConnections().stream()
				.filter(conn -> conn.getSource().equals(source))
				.collect(Collectors.toList());
		Type type = backend().types().connectionType(network, connections.get(0));
//		String size = connections.stream()
//				.map(c -> connectionSize(c).map(Object::toString).orElse("D"))
//				.collect(Collectors.joining("_"));
		String size = "D";
		return backend().code().type(type) + "_" + size;
	}

	default String targetEndTypeSize(Connection.End target) {
		Network network = backend().task().getNetwork();
		Connection connection = network.getConnections().stream()
				.filter(conn -> conn.getTarget().equals(target))
				.findFirst().get();
		Type type = backend().types().connectionType(network, connection);
		String size = connectionSize(connection).map(Object::toString).orElse("D");
		return backend().code().type(type) + "_" + size;
	}

	default Optional<Long> connectionSize(Connection connection) {
		return connection.getValueAttribute("bufferSize")
				.map(attr -> backend().constants().intValue(attr.getValue()).getAsLong());
	}

	default String sizeToString(int size) {
		if (size == 0) {
			return "D";
		} else if (size > 0) {
			return Integer.toString(size);
		} else {
			throw new IllegalArgumentException();
		}
	}

	default void channelCodeForType(Type type, int size) {
		String tokenType = backend().code().type(type);
		String sizeString = sizeToString(size);
		emitter().emit("// CHANNEL %s", type);
		emitter().emit("typedef struct {");
		emitter().emit("	size_t read;");
		emitter().emit("	size_t write;");
		emitter().emit("	%s *buffer;", tokenType);
		emitter().emit("} channel_%s_%s;", tokenType, sizeString);
		emitter().emit("");

		emitter().emit("static inline _Bool channel_has_data_%s_%s(channel_%1$s_%2$s *channel, size_t tokens) {", tokenType, sizeString);
		emitter().emit("	return channel->write - channel->read >= tokens;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline %s channel_peek_first_%1$s_%s(channel_%1$s_%2$s *channel) {", tokenType, sizeString);
		emitter().emit("	return channel->buffer[channel->read %% BUFFER_SIZE];");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline void channel_peek_%s_%s(channel_%1$s_%2$s *channel, size_t offset, size_t tokens, %1$s *result) {", tokenType, sizeString);
		emitter().emit("	%s *res = result;", tokenType);
		emitter().emit("	for (size_t i = 0; i < tokens; i++) {");
		emitter().emit("		res[i] = channel->buffer[(channel->read+i+offset) %% BUFFER_SIZE];");
		emitter().emit("	}");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static inline void channel_consume_%s_%s(channel_%1$s_%2$s *channel, size_t tokens) {", tokenType, sizeString);
		emitter().emit("	channel->read += tokens;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static void channel_create_%s_%s(channel_%1$s_%2$s *channel) {", tokenType, sizeString);
		emitter().emit("	%s *buffer = malloc(sizeof(%1$s)*BUFFER_SIZE);", tokenType);
		emitter().emit("	channel->read = 0;");
		emitter().emit("	channel->write = 0;");
		emitter().emit("	channel->buffer = buffer;");
		emitter().emit("}");
		emitter().emit("");


		emitter().emit("static void channel_destroy_%s_%s(channel_%1$s_%2$s *channel) {", tokenType, sizeString);
		emitter().emit("	free(channel->buffer);");
		emitter().emit("}");
		emitter().emit("");
	}

	default void inputActorCodeForType(Type type, int size) {
		String tokenType = backend().code().type(type);
		String typeSize = tokenType + "_" + sizeToString(size);

		emitter().emit("typedef struct {");
		emitter().emit("	size_t channelc;");
		emitter().emit("	channel_%s **channelv;", typeSize);
		emitter().emit("	FILE *stream;");
		emitter().emit("} input_actor_%s;", typeSize);
		emitter().emit("");

		emitter().emit("static input_actor_%s *input_actor_create_%1$s(FILE *stream, channel_%1$s *channel_vector[], size_t channel_count) {", typeSize);
		emitter().emit("    input_actor_%s *actor = malloc(sizeof(input_actor_%1$s));", typeSize);
		emitter().emit("    actor->channelv = channel_vector;");
		emitter().emit("    actor->channelc = channel_count;");
		emitter().emit("    actor->stream = stream;");
		emitter().emit("    return actor;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static void input_actor_destroy_%s(input_actor_%1$s *actor) {", typeSize);
		emitter().emit("    free(actor);");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static _Bool input_actor_run_%s(input_actor_%1$s *actor) {", typeSize);
		emitter().emit("	size_t tokens = 0;");
		emitter().emit("	size_t write = actor->channelv[0]->write;");
		emitter().emit("	for (size_t i = 0; i < actor->channelc; i++) {");
		emitter().emit("		channel_%s *c = actor->channelv[i];", typeSize);
		emitter().emit("		size_t this_tokens = (write - c->read);");
		emitter().emit("		if (this_tokens > tokens) { tokens = this_tokens; }");
		emitter().emit("	}");
		emitter().emit("	size_t space = BUFFER_SIZE - tokens;");
		emitter().emit("    if (space > 0) {");
		emitter().emit("        %s buf[space];", tokenType);
		emitter().emit("        space = fread(buf, sizeof(%s), space, actor->stream);", tokenType);
		emitter().emit("        if (space > 0) {");
		emitter().emit("			channel_write_%s(actor->channelv, actor->channelc, buf, space);", typeSize);
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

	default void outputActorCodeForType(Type type, int size) {
		String tokenType = backend().code().type(type);
		String typeSize = tokenType + "_" + sizeToString(size);

		emitter().emit("typedef struct {");
		emitter().emit("	channel_%s *channel;", typeSize);
		emitter().emit("	FILE *stream;");
		emitter().emit("} output_actor_%s;", typeSize);
		emitter().emit("");

		emitter().emit("static output_actor_%s *output_actor_create_%1$s(FILE *stream, channel_%1$s *channel) {", typeSize);
		emitter().emit("    output_actor_%s *actor = malloc(sizeof(output_actor_%1$s));", typeSize);
		emitter().emit("    actor->channel = channel;");
		emitter().emit("    actor->stream = stream;");
		emitter().emit("    return actor;");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static void output_actor_destroy_%s(output_actor_%1$s *actor) {", typeSize);
		emitter().emit("    free(actor);");
		emitter().emit("}");
		emitter().emit("");

		emitter().emit("static _Bool output_actor_run_%s(output_actor_%1$s* actor) {", typeSize);
		emitter().emit("    channel_%s *channel = actor->channel;", typeSize);
		emitter().emit("    if (channel->write > 0) {");
		emitter().emit("        fwrite(channel->buffer, sizeof(%s), channel->write, actor->stream);", tokenType);
		emitter().emit("        channel->write = 0;");
		emitter().emit("        return true;");
		emitter().emit("    } else {");
		emitter().emit("        return false;");
		emitter().emit("    }");
		emitter().emit("}");
		emitter().emit("");
	}
}
