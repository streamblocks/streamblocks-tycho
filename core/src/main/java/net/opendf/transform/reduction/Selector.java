package net.opendf.transform.reduction;

import java.util.Collection;

public interface Selector<T> {
	T select(Collection<T> collection);
}
