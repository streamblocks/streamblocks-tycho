package se.lth.cs.tycho.type;

import java.util.Objects;

public final class SetType implements CollectionType {

	private final Type elementType;

	public SetType(Type elementType) {
		this.elementType = elementType;
	}

	public Type getElementType() {
		return elementType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SetType setType = (SetType) o;
		return getElementType().equals(setType.getElementType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getElementType());
	}

	@Override
	public String toString() {
		return "Set(type:" + getElementType() + ")";
	}
}
