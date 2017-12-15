package se.lth.cs.tycho.attribute.util;

import java.util.Objects;

final class Pair<A, B> {
	private final A a;
	private final B b;
	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}
	public A a() { return a; }
	public B b() { return b; }

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof Pair) {
			Pair that = (Pair) o;
			return a.equals(that.a) && b.equals(that.b);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b);
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", a, b);
	}
}
