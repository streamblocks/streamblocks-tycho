/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.lth.cs.tycho.transform.reduction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import se.lth.cs.tycho.transform.reduction.util.Extremes;
import static org.junit.Assert.*;

/**
 *
 * @author gustav
 */
public class ExtremesTest {

	private <T> void testMin(List<T> input, Comparator<T> comparator, Collector<T, ?, List<T>> minCollector) {
		List<T> output = input.stream().collect(minCollector);
		Optional<T> min = input.stream().min(comparator);
		List<T> expected;
		if (min.isPresent()) {
			expected = input.stream().filter(x -> comparator.compare(x, min.get()) == 0).collect(Collectors.toList());
		} else {
			expected = Collections.emptyList();
		}
		assertEquals("Wrong output", expected, output);
	}

	private <T> void testMax(List<T> input, Comparator<T> comparator, Collector<T, ?, List<T>> maxCollector) {
		testMin(input, comparator.reversed(), maxCollector);
	}

	@Test
	public void testMin() {
		testMin(Arrays.asList(5, 3, 1, 2, 2, 5, 1, 1, 2, 5, 3), Comparator.naturalOrder(), Extremes.min());
	}

	@Test
	public void testMax() {
		testMax(Arrays.asList(5, 3, 1, 2, 2, 5, 1, 1, 2, 5, 3), Comparator.naturalOrder(), Extremes.max());
	}

	@Test
	public void testMinBy() {
		Comparator<String> comparator = Comparator.comparingInt(String::length);
		testMin(Arrays.asList("abc", "def", "s", "r", "fds"), comparator, Extremes.minBy(comparator));
	}

	@Test
	public void testMaxBy() {
		Comparator<String> comparator = Comparator.comparingInt(String::length);
		testMax(Arrays.asList("abc", "def", "s", "r", "fds"), comparator, Extremes.maxBy(comparator));
	}

	@Test
	public void testEmptyStream() {
		assertEquals("Wrong output",
				Collections.emptyList(),
				Stream.<String>empty().collect(Extremes.min()));
	}

	@Test
	public void testSingletonStream() {
		assertEquals("Wrong output",
				Collections.singletonList(5),
				Stream.of(5).collect(Extremes.min()));
	}
}
