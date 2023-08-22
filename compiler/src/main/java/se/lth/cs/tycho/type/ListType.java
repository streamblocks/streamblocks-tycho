package se.lth.cs.tycho.type;

import java.util.Objects;
import java.util.OptionalLong;

public final class ListType implements CollectionType {
	private final Type elementType;
	private final OptionalLong size;

	public ListType(Type elementType, OptionalLong size) {
		this.elementType = elementType;
		this.size = size;
	}

	public Type getElementType() {
		return elementType;
	}

	public OptionalLong getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "List(type:" + elementType + (size.isPresent() ? ", size=" + size.getAsLong() : "") + ")";
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
