package se.lth.cs.tycho.type;

import java.util.Objects;

public abstract class AlgebraicType implements Type {

	private final String name;

	public AlgebraicType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlgebraicType that = (AlgebraicType) o;
		return getName().equals(that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}

	@Override
	public String toString() {
		return getName();
	}
}
