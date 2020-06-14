package se.lth.cs.tycho.meta.interp.value;

import java.util.Objects;

public class ValueInteger implements Value {

	private final int integer;

	public ValueInteger(int integer) {
		this.integer = integer;
	}

	public int integer() {
		return integer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueInteger that = (ValueInteger) o;
		return integer == that.integer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(integer);
	}

	@Override
	public String toString() {
		return "" + integer();
	}
}
