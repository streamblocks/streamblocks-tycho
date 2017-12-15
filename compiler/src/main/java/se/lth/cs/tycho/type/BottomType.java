package se.lth.cs.tycho.type;

public enum BottomType implements Type {
	INSTANCE;

	@Override
	public String toString() {
		return "<bottom>";
	}
}
