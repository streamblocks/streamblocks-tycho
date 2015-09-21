package se.lth.cs.tycho.types;

public enum BottomType implements Type {
	INSTANCE;

	@Override
	public String toString() {
		return "<bottom>";
	}
}
