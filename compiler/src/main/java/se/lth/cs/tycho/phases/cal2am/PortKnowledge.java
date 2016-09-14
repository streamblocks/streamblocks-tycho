package se.lth.cs.tycho.phases.cal2am;


public final class PortKnowledge {
	private final int lowerBound;
	private final int upperBound;
	private static final int INFINITY = -1;

	public PortKnowledge(int lowerBound, int upperBound) {
		if (upperBound != INFINITY && upperBound < lowerBound || lowerBound < 0) {
			throw new IllegalArgumentException();
		}
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public static PortKnowledge nil() {
		return new PortKnowledge(0, INFINITY);
	}

	public PortKnowledge withUpperBound(int upperBound) {
		return new PortKnowledge(lowerBound, upperBound);
	}

	public PortKnowledge withoutUpperBound() {
		return new PortKnowledge(lowerBound, INFINITY);
	}

	public PortKnowledge withLowerBound(int lowerBound) {
		return new PortKnowledge(lowerBound, upperBound);
	}

	public PortKnowledge withoutLowerBound() {
		return new PortKnowledge(0, upperBound);
	}

	public PortKnowledge add(int n) {
		return new PortKnowledge(Math.addExact(lowerBound, n), upperBound == INFINITY ? INFINITY : Math.addExact(upperBound, n));
	}

	public Knowledge has(int n) {
		if (n <= lowerBound) {
			return Knowledge.TRUE;
		} else if (upperBound != INFINITY && n > upperBound) {
			return Knowledge.FALSE;
		} else {
			return Knowledge.UNKNOWN;
		}
	}

	public int lowerBound() {
		return lowerBound;
	}

	public int upperBound() {
		return upperBound;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PortKnowledge that = (PortKnowledge) o;

		if (lowerBound != that.lowerBound) return false;
		return upperBound == that.upperBound;

	}

	@Override
	public int hashCode() {
		int result = lowerBound;
		result = 31 * result + upperBound;
		return result;
	}

	public String toString() {
		return lowerBound + " .. " + (upperBound == INFINITY ? "inf" : upperBound);
	}
}
