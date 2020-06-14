package se.lth.cs.tycho.type;

import java.util.Objects;

public class AliasType implements Type {

	private final String name;
	private Type type;

	public AliasType(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public Type getConcreteType() {
		if (type instanceof AliasType) {
			return ((AliasType) type).getConcreteType();
		}
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AliasType aliasType = (AliasType) o;
		return getName().equals(aliasType.getName()) &&
				getType().equals(aliasType.getType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getType());
	}

	@Override
	public String toString() {
		return getName();
	}
}
