package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.stream.Collectors;

public class ValueMap extends Value {

	private final ImmutableList<ImmutableEntry<Value, Value>> mappings;

	public ValueMap(ImmutableList<ImmutableEntry<Value, Value>> mappings) {
		this.mappings = mappings;
	}

	public ImmutableList<ImmutableEntry<Value, Value>> mappings() {
		return mappings;
	}

	@Override
	public String toString() {
		return mappings().stream().map(mapping -> mapping.getKey() + " -> " + mapping.getValue()).collect(Collectors.joining(", ", "map {", "}"));
	}
}
