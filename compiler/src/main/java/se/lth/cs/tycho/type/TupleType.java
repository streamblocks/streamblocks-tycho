package se.lth.cs.tycho.type;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

	@Override
	public String toString() {
		return types.stream().map(Type::toString).collect(Collectors.joining(", ", "(", ")"));
	}
}
