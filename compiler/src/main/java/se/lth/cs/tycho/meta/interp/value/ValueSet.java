package se.lth.cs.tycho.meta.interp.value;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ValueSet implements Value {

	private final Set<Value> elements;

	public ValueSet(Set<Value> elements) {
		this.elements = elements;
	}

	public Set<Value> elements() {
		return elements;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueSet valueSet = (ValueSet) o;
		return elements.equals(valueSet.elements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elements);
	}

	@Override
	public String toString() {
		return elements.stream().map(Value::toString).collect(Collectors.joining(", ", "{", "}"));
	}
}
