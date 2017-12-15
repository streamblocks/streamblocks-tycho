package se.lth.cs.tycho.attribute.util;

import java.util.function.Supplier;

final class IdentityBox<T> implements Supplier<T> {
	private final T object;

	public IdentityBox(T object) {
		this.object = object;
	}

	public T get() {
		return object;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof IdentityBox) {
			IdentityBox that = (IdentityBox) o;
			return this.object == that.object;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(object);
	}

	@Override
	public String toString() {
		return object.toString();
	}
}
