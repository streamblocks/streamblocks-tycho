package se.lth.cs.tycho.types;

import java.util.OptionalInt;

public class ListType implements Type {
	private final Type elementType;
	private final OptionalInt size;

	public ListType(Type elementType, OptionalInt size) {
		this.elementType = elementType;
		this.size = size;
	}

	// Treating Optional.empty() as infinity.
	private boolean greaterOrEqual(OptionalInt a, OptionalInt b) {
		if (!a.isPresent()) return true;
		if (!b.isPresent()) return false;
		return a.getAsInt() >= b.getAsInt();
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
}
