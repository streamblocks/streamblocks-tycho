package se.lth.cs.tycho.meta.interp;

import se.lth.cs.tycho.meta.interp.value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Environment {

	private final Map<String, Value> bindings;

	public Environment() {
		this(new HashMap<>());
	}

	private Environment(Map<String, Value> bindings) {
		this.bindings = bindings;
	}

	public Optional<Value> get(String symbol) {
		return Optional.ofNullable(bindings.get(symbol));
	}

	public void put(String symbol, Value value) {
		bindings.put(symbol, value);
	}

	public Environment with(Map<String, Value> bindings) {
		Map<String, Value> newBindings = new HashMap<>(this.bindings);
        newBindings.putAll(bindings);
		return new Environment(newBindings);
	}
}
