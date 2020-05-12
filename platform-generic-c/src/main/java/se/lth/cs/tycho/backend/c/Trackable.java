package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.AliasType;
import se.lth.cs.tycho.type.ListType;
import se.lth.cs.tycho.type.TupleType;
import se.lth.cs.tycho.type.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Module
public interface Trackable {

	@Binding(BindingKind.INJECTED)
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	@Binding(BindingKind.LAZY)
	default Stack<Map<String, Type>> pointers() { return new Stack<>(); }

	default void enter() {
		pointers().push(new HashMap<>());
	}

	default void exit() {
		pointers().pop().forEach((ptr, type) -> release(ptr, type));
	}

	default void track(String ptr, Type type) {
		if (!(isTrackable(type))) {
			return;
		}
		if (!(pointers().empty())) {
			pointers().peek().put(ptr, type);
		}
	}

	default void release(String ptr, Type type) {

	}

	default void release(String ptr, AlgebraicType type) {
		emitter().emit("%s(%s);", backend().algebraic().utils().destructor(type), ptr);
	}

	default void release(String ptr, ListType type) {
		emitter().emit("for (size_t i = 0; i < %s; ++i) {", type.getSize().getAsInt());
		emitter().increaseIndentation();
		release(String.format("%s.data[i]", ptr), type.getElementType());
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void release(String ptr, TupleType type) {
		release(ptr, backend().tuples().convert().apply(type));
	}

	default void release(String ptr, AliasType type) {
		release(ptr, type.getConcreteType());
	}

	default boolean isTrackable(Type type) {
		return false;
	}

	default boolean isTrackable(AlgebraicType type) {
		return true;
	}

	default boolean isTrackable(ListType type) {
		return isTrackable(type.getElementType());
	}

	default boolean isTrackable(TupleType type) {
		return true;
	}

	default boolean isTrackable(AliasType type) {
		return isTrackable(type.getConcreteType());
	}
}
