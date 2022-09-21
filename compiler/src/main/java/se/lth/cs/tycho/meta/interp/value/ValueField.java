package se.lth.cs.tycho.meta.interp.value;

import java.util.Objects;

public class ValueField extends Value {

	private final String name;
	private final Value value;

	public ValueField(String name, Value value) {
		this.name = name;
		this.value = value;
	}

	public String name() {
		return name;
	}

	public Value value() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueField that = (ValueField) o;
		return name.equals(that.name) &&
				value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public String toString() {
		return name() + ":" + value();
	}
}
