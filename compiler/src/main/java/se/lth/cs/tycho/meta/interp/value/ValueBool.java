package se.lth.cs.tycho.meta.interp.value;

import java.util.Objects;

public class ValueBool implements Value {

	private final boolean bool;

	public ValueBool(boolean bool) {
		this.bool = bool;
	}

	public boolean bool() {
		return bool;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueBool valueBool = (ValueBool) o;
		return bool == valueBool.bool;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bool);
	}

	@Override
	public String toString() {
		return "" + bool();
	}
}
