package se.lth.cs.tycho.type;

import java.util.OptionalInt;

public class RangeType implements Type {
	private final Type type;
	private final OptionalInt length;

	public RangeType(Type type, OptionalInt length) {
		this.type = type;
		this.length = length;
	}

	public Type getType() {
		return type;
	}

	public OptionalInt getLength() {
		return length;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Range(");
		builder.append("type:");
		builder.append(type.toString());
		if (length.isPresent()) {
			builder.append("length=");
			builder.append(length.getAsInt());
		}
		builder.append(")");
		return builder.toString();
	}
}
