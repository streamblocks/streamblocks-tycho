package net.opendf.ir.util;

import java.util.Collections;
import java.util.List;

public class Lists {
	/**
	 * Checks whether the two iterables creates iterators with the same elements
	 * in the same order. Nulls are treated as empty iterables.
	 * 
	 * @param a
	 * @param b
	 * @return true iff both a and b is empty, or both a and have the same length and the elements with the same index are equal.
	 */
	public static <E> boolean equals(List<? extends E> a, List<? extends E> b) {
		if (a == null)
			a = Collections.emptyList();
		if (b == null)
			b = Collections.emptyList();
		return a.equals(b);
	}

}
