package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;
import java.util.stream.Collectors;

public class ValueTuple extends Value {

	private final ImmutableList<Value> elements;

	public ValueTuple(ImmutableList<Value> elements) {
		this.elements = elements;
	}

	public ImmutableList<Value> elements() {
		return elements;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueTuple that = (ValueTuple) o;
		return elements.equals(that.elements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elements);
	}

	@Override
	public String toString() {
		return elements.stream().map(Value::toString).collect(Collectors.joining(", ", "(", ")"));
	}
}
