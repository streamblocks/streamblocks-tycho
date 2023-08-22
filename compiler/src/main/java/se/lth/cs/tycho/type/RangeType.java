package se.lth.cs.tycho.type;

import java.util.OptionalInt;
import java.util.OptionalLong;

public class RangeType implements CollectionType {
	private final Type type;
	private final OptionalLong length;

	public RangeType(Type type, OptionalLong length) {
		this.type = type;
		this.length = length;
	}

	public Type getType() {
		return type;
	}

	public OptionalLong getLength() {
		return length;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Range(");
		builder.append("type:");
		builder.append(type.toString());
		if (length.isPresent()) {
			builder.append("length=");
			builder.append(length.getAsLong());
		}
		builder.append(")");
		return builder.toString();
	}
}
