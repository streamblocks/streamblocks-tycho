package se.lth.cs.tycho.type;

import java.util.Objects;

public class FieldType {

	private final String name;
	private final Type type;

	public FieldType(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FieldType fieldType = (FieldType) o;
		return getName().equals(fieldType.getName()) &&
				getType().equals(fieldType.getType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getType());
	}
}
