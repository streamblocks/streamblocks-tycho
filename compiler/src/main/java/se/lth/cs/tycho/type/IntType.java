package se.lth.cs.tycho.type;

import java.util.Objects;
import java.util.OptionalInt;

public final class IntType implements Type {
	private final OptionalInt size;
	private final boolean signed;

	public IntType(OptionalInt size, boolean signed) {
		this.size = size;
		this.signed = signed;
	}

	@Override
	public String toString() {
		return (signed ? "int" : "uint") + (size.isPresent() ? "(size=" + size.getAsInt() + ")" : "");
	}

	public OptionalInt getSize() {
		return size;
	}

	public boolean isSigned() {
		return signed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IntType intType = (IntType) o;
		return signed == intType.signed &&
				Objects.equals(size, intType.size);
	}

	@Override
	public int hashCode() {
		return Objects.hash(size, signed);
	}
}
