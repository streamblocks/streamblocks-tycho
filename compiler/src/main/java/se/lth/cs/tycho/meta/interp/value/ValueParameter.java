package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;
import java.util.Optional;

public class ValueParameter implements Value {

	private final TypeExpr type;
	private final String name;
	private final Optional<Value> defaultValue;

	public ValueParameter(TypeExpr type, String name, Optional<Value> defaultValue) {
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public TypeExpr type() {
		return type;
	}

	public String name() {
		return name;
	}

	public Optional<Value> defaultValue() {
		return defaultValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueParameter that = (ValueParameter) o;
		return type.equals(that.type) &&
				name.equals(that.name) &&
				defaultValue.equals(that.defaultValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, defaultValue);
	}

	@Override
	public String toString() {
		return name() + defaultValue().map(value -> ":" + value).orElse("");
	}
}
