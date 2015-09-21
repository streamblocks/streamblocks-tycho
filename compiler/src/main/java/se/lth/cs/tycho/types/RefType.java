package se.lth.cs.tycho.types;

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
}
