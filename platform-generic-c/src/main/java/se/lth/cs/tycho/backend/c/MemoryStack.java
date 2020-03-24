package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.ListType;
import se.lth.cs.tycho.type.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@Module
public interface MemoryStack {

	@Binding(BindingKind.INJECTED)
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	@Binding(BindingKind.LAZY)
	default Stack<Map<String, Type>> pointers() { return new Stack<>(); }

	default void enterScope() {
		pointers().push(new HashMap<>());
	}

	default void exitScope() {
		pointers().pop().forEach((ptr, type) -> {
			if (type instanceof AlgebraicType) {
				emitter().emit("%s(%s);", backend().algebraicTypes().destructor((AlgebraicType) type), ptr);
			} else if (type instanceof ListType) {
				ListType listType = (ListType) type;
				emitter().emit("for (size_t i = 0; i < %s; ++i) {", listType.getSize().getAsInt());
				emitter().increaseIndentation();
				emitter().emit("%s(%s.data[i]);", backend().algebraicTypes().destructor((AlgebraicType) listType.getElementType()), ptr);
				emitter().decreaseIndentation();
				emitter().emit("}");
			}
		});
	}
	
	default void trackPointer(String ptr, Type type) {
		if (!pointers().empty()) {
			pointers().peek().put(ptr, type);
		}
	}
}
