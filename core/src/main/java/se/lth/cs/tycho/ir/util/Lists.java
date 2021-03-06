package se.lth.cs.tycho.ir.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Lists {
	/**
	 * Checks whether the two iterables creates iterators with the same elements
	 * in the same order. Nulls are treated as empty iterables.
	 * 
	 * @param <E> a common supertype for the elements
	 * @param a a list
	 * @param b a list
	 * @return true iff both a and b is empty, or both a and have the same length and the elements with the same index are equal.
	 */
	public static <E> boolean equals(List<? extends E> a, List<? extends E> b) {
		if (a == null)
			a = Collections.emptyList();
		if (b == null)
			b = Collections.emptyList();
		return a.equals(b);
	}

	public static boolean sameElements(List<?> a, List<?> b) {
		Iterator aa = a == null ? Collections.emptyIterator() : a.iterator();
		Iterator bb = b == null ? Collections.emptyIterator() : b.iterator();
		while (aa.hasNext() && bb.hasNext()) {
			if (aa.next() != bb.next()) {
				return false;
			}
		}
		return !aa.hasNext() && !bb.hasNext();
	}

}
