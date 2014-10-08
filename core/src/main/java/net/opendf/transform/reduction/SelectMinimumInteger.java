package net.opendf.transform.reduction;

import java.util.Collection;
import java.util.Iterator;

public class SelectMinimumInteger implements Selector<Integer> {

	@Override
	public Integer select(Collection<Integer> collection) {
		Iterator<Integer> iter = collection.iterator();
		if (iter.hasNext()) {
			int min = iter.next();
			while (iter.hasNext()) {
				int x = iter.next();
				if (x < min) {
					min = x;
				}
			}
			return min;
		} else {
			return null;
		}
	}

}
