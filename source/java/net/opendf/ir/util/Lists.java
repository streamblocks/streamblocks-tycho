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
	 * @return
	 */
	public static <E> boolean equals(List<? extends E> a, List<? extends E> b) {
		if (a == null)
			a = Collections.emptyList();
		if (b == null)
			b = Collections.emptyList();
		return a.equals(b);
	}

}
