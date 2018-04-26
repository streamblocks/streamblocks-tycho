package se.lth.cs.tycho.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A collection of collectors, similar to java.util.stream.Collectors.
 */
public final class TychoCollectors {
	private TychoCollectors() {}

	/**
	 * A collector that collect all minimal elements of a stream.
	 * @param comparator element comparator
	 * @param downstream downstream collector
	 * @param <T> element type
	 * @param <R> result type
	 * @return a minimal element collector
	 */
	public static <T, R> Collector<T, ?, R> minimaBy(Comparator<T> comparator, Collector<T, ?, R> downstream) {
		Collector<T, MinColl<T>, MinColl<T>> coll = Collector.of(() -> new MinColl<>(comparator), MinColl::add, MinColl::combineWith);
		return java.util.stream.Collectors.collectingAndThen(coll, minColl -> minColl.stream().collect(downstream));
	}

	/**
	 * A collector that collect all maximal elements of a stream.
	 * @param comparator element comparator
	 * @param downstream downstream collector
	 * @param <T> element type
	 * @param <R> result type
	 * @return a maximal element collector
	 */
	public static <T, R> Collector<T, ?, R> maximaBy(Comparator<T> comparator, Collector<T, ?, R> downstream) {
		return minimaBy(comparator.reversed(), downstream);
	}

	private static class MinColl<E> {
		private final Comparator<E> comparator;
		private final ArrayList<E> elements;
		MinColl(Comparator<E> comparator) {
			this.comparator = comparator;
			this.elements = new ArrayList<>();
		}

		Stream<E> stream() {
			return elements.stream();
		}

		void add(E element) {
			if (elements.isEmpty()) {
				elements.add(element);
			} else {
				int cmp = comparator.compare(elements.get(0), element);
				if (cmp == 0) {
					elements.add(element);
				} else if (cmp > 0) {
					elements.clear();
					elements.add(element);
				}
			}
		}

		MinColl<E> combineWith(MinColl<E> that) {
			if (this.elements.isEmpty()) {
				return that;
			} else if (that.elements.isEmpty()) {
				return this;
			} else {
				E thisFirst = this.elements.get(0);
				E thatFirst = that.elements.get(0);
				int cmp = comparator.compare(thisFirst, thatFirst);
				if (cmp == 0) {
					this.elements.addAll(that.elements);
					return this;
				} else if (cmp < 0) {
					return this;
				} else {
					return that;
				}
			}
		}
	}
}
