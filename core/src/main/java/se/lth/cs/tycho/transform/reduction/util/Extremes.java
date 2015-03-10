/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.lth.cs.tycho.transform.reduction.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collectors for collecting all minimal or maximal elements of a stream. The
 * main difference to Collectors.minBy and Collectors.maxBy is that those
 * collectors only collects one minimal or maximal element, while these
 * collectors collect all minimal or maximal elements.
 *
 * @author gustav
 */
public class Extremes {

	public static <T extends Comparable<? super T>> Collector<T, ?, List<T>> min() {
		return new MinCollector<T>(Comparator.naturalOrder());
	}

	public static <T> Collector<T, ?, List<T>> minBy(Comparator<T> comparator) {
		return new MinCollector<>(comparator);
	}

	public static <T extends Comparable<? super T>> Collector<T, ?, List<T>> max() {
		return new MinCollector<>(Comparator.<T>naturalOrder().reversed());
	}

	public static <T> Collector<T, ?, List<T>> maxBy(Comparator<T> comparator) {
		return new MinCollector<>(comparator.reversed());
	}

	private static class MinCollector<T> implements Collector<T, List<T>, List<T>> {

		private final Comparator<T> comparator;

		private MinCollector(Comparator<T> comparator) {
			this.comparator = comparator;
		}

		@Override
		public Supplier<List<T>> supplier() {
			return ArrayList::new;
		}

		@Override
		public BiConsumer<List<T>, T> accumulator() {
			return (List<T> list, T val) -> {
				if (list.isEmpty()) {
					list.add(val);
				} else {
					T min = list.get(0);
					int minCmpVal = comparator.compare(min, val);
					if (minCmpVal == 0) {
						list.add(val);
					} else if (minCmpVal > 0) {
						list.clear();
						list.add(val);
					}
				}
			};
		}

		@Override
		public BinaryOperator<List<T>> combiner() {
			return (List<T> a, List<T> b) -> {
				if (a.isEmpty()) {
					return b;
				} else if (b.isEmpty()) {
					return a;
				} else {
					int aCmpB = comparator.compare(a.get(0), b.get(0));
					if (aCmpB == 0) {
						a.addAll(b); // not b.addAll(a) because of object order
						return a;
					} else if (aCmpB < 0) {
						return a;
					} else {
						return b;
					}
				}
			};
		}

		@Override
		public Function<List<T>, List<T>> finisher() {
			return Function.identity();
		}

		private static final Set<Characteristics> characteristics = EnumSet.of(Characteristics.IDENTITY_FINISH);

		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}
	}
}
