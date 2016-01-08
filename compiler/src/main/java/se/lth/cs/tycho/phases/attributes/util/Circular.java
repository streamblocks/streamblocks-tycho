package se.lth.cs.tycho.phases.attributes.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

final class Circular<R> implements Supplier<R> {
	private final R bottom;
	private final Supplier<R> definition;
	private static final ThreadLocal<Evaluator> evaluator = ThreadLocal.withInitial(Evaluator::new);
	private final ThreadLocal<Value<R>> value;
	private final AtomicReference<R> finalValue;

	public Circular(R bottom, Supplier<R> definition) {
		this.bottom = bottom;
		this.definition = definition;
		this.value = ThreadLocal.withInitial(() -> new Value<>(bottom));
		this.finalValue = new AtomicReference<>();
	}

	@Override
	public R get() {
		R finVal = finalValue.get();
		if (finVal != null) {
			return finVal;
		}
		Evaluator eval = evaluator.get();
		if (eval.active) {
			return eval.get(this);
		} else {
			return eval.start(this);
		}
	}

	private static class Value<R> {
		private long version;
		private R value;

		public Value(R value) {
			this.version = 0;
			this.value = value;
		}
	}

	private static class Evaluator {
		private long version = 0;
		private boolean active = false;

		public <R> R start(Circular<R> circ) {
			active = true;
			Value value = circ.value.get();
			R computed = circ.bottom;
			do {
				version = version + 1;
				value.version = version;
				value.value = computed;
				computed = circ.definition.get();
			} while (!computed.equals(value.value));
			active = false;
			circ.finalValue.compareAndSet(null, computed);
			return circ.finalValue.get();
		}

		public <R> R get(Circular<R> circ) {
			Value<R> value = circ.value.get();
			if (value.version != version) {
				value.version = version;
				value.value = circ.definition.get();
			}
			return value.value;
		}
	}

}
