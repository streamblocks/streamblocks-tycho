package se.lth.cs.tycho.types;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.Objects;

public final class TupleType implements Type {
	private final ImmutableList<Type> types;

	public TupleType(List<Type> types) {
		this.types = ImmutableList.from(types);
	}

	public ImmutableList<Type> getTypes() {
		return types;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TupleType tupleType = (TupleType) o;
		return Objects.equals(types, tupleType.types);
	}

	@Override
	public int hashCode() {
		return Objects.hash(types);
	}
}