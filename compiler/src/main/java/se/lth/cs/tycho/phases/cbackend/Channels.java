package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.types.IntType;
import se.lth.cs.tycho.types.Type;

import java.util.OptionalInt;

@Module
public interface Channels {
	@Binding Backend backend();
	default Emitter emitter() { return backend().emitter(); }

	void channelCodeForType(Type type, int size);
	void channelListCodeForType(Type type, int size);
	void inputActorCodeForType(Type type, int size);
	void outputActorCodeForType(Type type, int size);

	default void outputActorCode() {
		backend().task().getNetwork().getOutputPorts().stream()
				.map(backend().types()::declaredPortType)
				.distinct()
				.forEach((type) -> outputActorCodeForType(type, 0));
	}

	default void inputActorCode() {
		backend().task().getNetwork().getInputPorts().stream()
				.map(backend().types()::declaredPortType)
				.distinct()
				.forEach((type) -> inputActorCodeForType(type, 0));
	}

	default void fifo_h() {
		emitter().emit("#include <stdint.h>");
		emitter().emit("");
		emitter().emitRawLine("#ifndef BUFFER_SIZE\n" +
				"#define BUFFER_SIZE 256\n" +
				"#endif\n");
		channelCode();
	}

	default void channelCode() {
		Network network = backend().task().getNetwork();
		network.getConnections().stream()
				.map(connection -> backend().types().connectionType(network, connection))
				.map(this::intToNearest8Mult)
				.distinct()
				.forEach(type -> {
					channelCodeForType(type, 0);
					channelListCodeForType(type, 0);
				});
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

}
