package se.lth.cs.tycho.type;

import java.util.Objects;
import java.util.OptionalInt;

public final class ListType implements CollectionType {
	private final Type elementType;
	private final OptionalInt size;

	public ListType(Type elementType, OptionalInt size) {
		this.elementType = elementType;
		this.size = size;
	}

	public Type getElementType() {
		return elementType;
	}

	public OptionalInt getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "List(type:" + elementType + (size.isPresent() ? ", size=" + size.getAsInt() : "") + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ListType listType = (ListType) o;
		return Objects.equals(elementType, listType.elementType) &&
				Objects.equals(size, listType.size);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elementType, size);
	}
}
