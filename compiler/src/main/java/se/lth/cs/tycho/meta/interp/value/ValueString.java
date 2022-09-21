package se.lth.cs.tycho.meta.interp.value;

import java.util.Objects;

public class ValueString extends Value {

	private final String string;

	public ValueString(String string) {
		this.string = string;
	}

	public String string() {
		return string;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueString that = (ValueString) o;
		return string.equals(that.string);
	}

	@Override
	public int hashCode() {
		return Objects.hash(string);
	}

	@Override
	public String toString() {
		return string();
	}
}
