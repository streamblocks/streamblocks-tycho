package se.lth.cs.tycho.type;

import java.util.Objects;

public class RefType implements Type {
	private final Type type;

	public RefType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Reference(type=" + type + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RefType)) return false;
		RefType that = (RefType) o;
		return Objects.equals(this.type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}
}
