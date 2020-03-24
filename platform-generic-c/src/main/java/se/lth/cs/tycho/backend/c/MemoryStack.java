package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.Type;

import java.util.HashMap;
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
			emitter().emit("%s(%s);", backend().algebraicTypes().destructor((AlgebraicType) type), ptr);
		});
	}
	
	default void trackPointer(String ptr, Type type) {
		if (!pointers().empty()) {
			pointers().peek().put(ptr, type);
		}
	}
}
