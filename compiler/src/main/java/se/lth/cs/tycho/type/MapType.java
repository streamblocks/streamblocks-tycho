package se.lth.cs.tycho.type;

import java.util.Objects;

public final class MapType implements CollectionType {

	private final Type keyType;
	private final Type valueType;

	public MapType(Type keyType, Type valueType) {
		this.keyType = keyType;
		this.valueType = valueType;
	}

	public Type getKeyType() {
		return keyType;
	}

	public Type getValueType() {
		return valueType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MapType mapType = (MapType) o;
		return getKeyType().equals(mapType.getKeyType()) &&
				getValueType().equals(mapType.getValueType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getKeyType(), getValueType());
	}

	@Override
	public String toString() {
		return String.format("Map(key:%s, value:%s)", getKeyType(), getValueType());
	}
}
