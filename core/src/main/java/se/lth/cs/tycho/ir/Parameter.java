package se.lth.cs.tycho.ir;

import java.util.Objects;

public class Parameter<T> extends AbstractIRNode {
	private final String name;
	private final T value;

	public Parameter(String name, T value) {
		this(null, name, value);
	}

	public Parameter(IRNode original, String name, T value) {
		super(original);
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}
	
	public static <T> Parameter<T> of(String name, T value) {
		return new Parameter<>(name, value);
	}

	public Parameter<T> copy(String name, T value) {
		if (Objects.equals(this.name, name) && Objects.equals(this.value, value)) {
			return this;
		} else {
			return new Parameter<>(this, name, value);
		}
	}
}
