package se.lth.cs.tycho.types;

import java.util.OptionalInt;

public class IntType implements Type {
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
}
