package net.opendf.transform.caltoam.util;

import static java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opendf.ir.common.QID;

public class QIDMap<V> {
	List<Map.Entry<QID, V>> map;
	Set<V> tagless;

	public QIDMap() {
		map = new ArrayList<Map.Entry<QID, V>>();
		tagless = new HashSet<V>();
	}

	public void put(QID qid, V value) {
		if (qid != null) {
			map.add(new SimpleImmutableEntry<QID, V>(qid, value));
		} else {
			tagless.add(value);
		}
	}
	
	public Set<V> get(QID qid) {
		Set<V> set = new HashSet<V>();
		for (Map.Entry<QID, V> entry : map) {
			if (qid.isPrefixOf(entry.getKey())) {
				set.add(entry.getValue());
			}
		}
		return set;
	}
	
	public Set<V> getTagLess() {
		return Collections.unmodifiableSet(tagless);
	}

}
