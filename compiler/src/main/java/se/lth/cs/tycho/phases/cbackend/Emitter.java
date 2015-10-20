package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

@Module
public interface Emitter {

	@Binding(BindingKind.INJECTED)
	PrintWriter writer();

	@Binding
	default AtomicInteger indentation() {
		return new AtomicInteger(0);
	}

	default void increaseIndentation() {
		indentation().incrementAndGet();
	}

	default void decreaseIndentation() {
		indentation().decrementAndGet();
	}

	default void emit(String format, Object... values) {
		if (!format.isEmpty()) {
			int i = indentation().get();
			while (i > 0) {
				writer().print('\t');
				i--;
			}
			writer().printf(format, values);
		}
		writer().println();
	}

	default void emitRawText(CharSequence text) {
		writer().println(text);
	}
}
