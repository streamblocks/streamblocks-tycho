package se.lth.cs.tycho.meta.interp.value;

import java.util.Objects;

public class ValueLong implements Value {

	private final long value;

	public ValueLong(long integer) {
		this.value = integer;
	}

	public long value() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueLong that = (ValueLong) o;
		return value == that.value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "" + value();
	}
}
