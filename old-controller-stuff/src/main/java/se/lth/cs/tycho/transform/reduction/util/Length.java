package se.lth.cs.tycho.transform.reduction.util;

import java.util.OptionalDouble;

public final class Length {
	private static final Comparator COMPARATOR = new Comparator();
	private final OptionalDouble actionProbability;
	private final int nbrOfTests;

	private Length(OptionalDouble actionProbability, int nbrOfTests) {
		this.actionProbability = actionProbability;
		this.nbrOfTests = nbrOfTests;
	}

	public static Length waitLength() {
		return new Length(OptionalDouble.empty(), 0);
	}

	public static Length execLength(double probability) {
		return new Length(OptionalDouble.of(probability), 0);
	}

	public static Length testLength(Length old) {
		return new Length(old.actionProbability, old.nbrOfTests + 1);
	}
	
	public static Comparator comparator() {
		return COMPARATOR;
	}

	public static class Comparator implements java.util.Comparator<Length> {
		@Override
		public int compare(Length left, Length right) {
			if (left.actionProbability.isPresent() && right.actionProbability.isPresent()) {
				int cmp = Double.compare(left.actionProbability.getAsDouble(), right.actionProbability.getAsDouble());
				if (cmp == 0) {
					return Integer.compare(left.nbrOfTests, right.nbrOfTests);
				} else {
					return -cmp;
				}
			} else if (left.actionProbability.isPresent()) {
				return -1;
			} else if (right.actionProbability.isPresent()) {
				return 1;
			} else {
				return Integer.compare(left.nbrOfTests, right.nbrOfTests);
			} 
		}
	}
}
