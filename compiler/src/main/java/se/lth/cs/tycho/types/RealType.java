package se.lth.cs.tycho.types;

public class RealType implements Type {
	private final int size;

	public static final RealType f32 = new RealType(32);
	public static final RealType f64 = new RealType(64);

	private RealType(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public static RealType of(int size) {
		switch(size) {
			case 32: return f32;
			case 64: return f64;
			default: throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		return String.format("real(%d)", size);
	}
}
