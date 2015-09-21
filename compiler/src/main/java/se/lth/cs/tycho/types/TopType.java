package se.lth.cs.tycho.types;

public enum TopType implements Type {
	INSTANCE;

	@Override
	public String toString() {
		return "<top>";
	}
}
