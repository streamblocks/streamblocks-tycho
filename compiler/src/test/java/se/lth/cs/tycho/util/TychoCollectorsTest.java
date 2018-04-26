package se.lth.cs.tycho.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TychoCollectorsTest {
	@Test
	public void minimaBy() throws Exception {
		List<Integer> integers = Arrays.asList(1, 3, 4, 2, 5, 3, 1, 1, 1, 2);
		testMinimaBy(integers, Comparator.naturalOrder());
		List<String> strings = Arrays.asList("ab", "sr", "eft", "fdd");
		testMinimaBy(strings, Comparator.comparingInt(String::length));
		List<Double> empty = Collections.emptyList();
		testMinimaBy(empty, Comparator.naturalOrder());
	}

	private <E> void testMinimaBy(List<E> list, Comparator<E> comparator) {
		Optional<E> min = list.stream().min(comparator);
		List<E> actual = list.stream().collect(TychoCollectors.minimaBy(comparator, Collectors.toList()));
		List<E> expected;
		if (min.isPresent()) {
			expected = list.stream().filter(e -> comparator.compare(min.get(), e) == 0).collect(Collectors.toList());
		} else {
			expected = Collections.emptyList();
		}
		assertEquals(expected, actual);
	}

}