package se.lth.cs.tycho.phases.attributes.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class Cached<T, R> implements Function<T, R> {
	private final ConcurrentHashMap<T, R> cache;
	private final Function<T, R> definition;

	public Cached(Function<T, R> definition) {
		this.cache = new ConcurrentHashMap<>();
		this.definition = definition;
	}

	public R apply(T t) {
		return cache.computeIfAbsent(t, definition);
	}
}
