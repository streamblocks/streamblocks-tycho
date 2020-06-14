package se.lth.cs.tycho.meta.interp.value;

import java.util.List;
import java.util.stream.Collectors;

public class ValueList implements Value {

	private final List<Value> elements;

	public ValueList(List<Value> elements) {
		this.elements = elements;
	}

	public List<Value> elements() {
		return elements;
	}

	@Override
	public String toString() {
		return elements.stream().map(Value::toString).collect(Collectors.joining(", ", "[", "]"));
	}
}
