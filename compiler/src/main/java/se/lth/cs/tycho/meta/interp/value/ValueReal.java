package se.lth.cs.tycho.meta.interp.value;

import java.util.Objects;

public class ValueReal extends Value {

	private final double real;

	public ValueReal(double real) {
		this.real = real;
	}

	public double real() {
		return real;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueReal valueReal = (ValueReal) o;
		return Double.compare(valueReal.real, real) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(real);
	}

	@Override
	public String toString() {
		return "" + real();
	}
}
