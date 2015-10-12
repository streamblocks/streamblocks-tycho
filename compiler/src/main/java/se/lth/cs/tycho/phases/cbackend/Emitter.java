package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;

import java.util.concurrent.atomic.AtomicInteger;

@Module
public interface Emitter {
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
				System.out.print('\t');
				i--;
			}
			System.out.printf(format, values);
		}
		System.out.println();
	}
}
