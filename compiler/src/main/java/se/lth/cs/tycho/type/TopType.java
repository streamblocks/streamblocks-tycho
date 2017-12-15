package se.lth.cs.tycho.type;

public enum TopType implements Type {
	INSTANCE;

	@Override
	public String toString() {
		return "<top>";
	}
}
