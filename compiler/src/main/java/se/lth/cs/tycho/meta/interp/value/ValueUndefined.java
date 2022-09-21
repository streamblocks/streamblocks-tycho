package se.lth.cs.tycho.meta.interp.value;

public class ValueUndefined extends Value {

	private static final ValueUndefined undefined = new ValueUndefined();
	private ValueUndefined() {}

	public static ValueUndefined undefined() {
		return undefined;
	}

	@Override
	public String toString() {
		return "<undefined>";
	}
}
