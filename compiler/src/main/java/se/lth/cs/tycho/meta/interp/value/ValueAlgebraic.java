package se.lth.cs.tycho.meta.interp.value;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ValueAlgebraic extends Value {

	private final String name;
	private final List<ValueField> fields;

	public ValueAlgebraic(String name, List<ValueField> fields) {
		this.name = name;
		this.fields = fields;
	}

	public String name() {
		return name;
	}

	public List<ValueField> fields() {
		return fields;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueAlgebraic that = (ValueAlgebraic) o;
		return name.equals(that.name) &&
				fields.equals(that.fields);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, fields);
	}

	@Override
	public String toString() {
		return name + fields().stream().map(Value::toString).collect(Collectors.joining(", ", "(", ")"));

	}
}
