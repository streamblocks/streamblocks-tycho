package net.opendf.ir.util;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

public class ImmutableEntry<K, V> extends SimpleImmutableEntry<K, V> {

	public ImmutableEntry(K key, V value) {
		super(key, value);
	}
	
	public ImmutableEntry(Entry<? extends K, ? extends V> entry) {
		super(entry.getKey(), entry.getValue());
	}
	
	public static <K, V> ImmutableEntry<K, V> of(K key, V value) {
		return new ImmutableEntry<K, V>(key, value);
	}
	
	public static <K, V> ImmutableEntry<K, V> copyOf(Entry<? extends K, ? extends V> entry) {
		return new ImmutableEntry<K, V>(entry.getKey(), entry.getValue());
	}
	
	public ImmutableEntry<K, V> copy(K key, V value) {
		if (key == getKey() && value == getValue()) return this;
		return new ImmutableEntry<K, V>(key, value);
	}
}
