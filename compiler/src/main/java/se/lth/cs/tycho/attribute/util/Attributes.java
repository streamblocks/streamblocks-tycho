package se.lth.cs.tycho.attribute.util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Attributes {

	public static <R> Supplier<R> circular(R bottom, Supplier<R> definition) {
		return new Circular<>(bottom, definition);
	}

	public static <T, R> Function<T, R> circular(R bottom, Function<T, R> definition) {
		Cached<T, Circular<R>> cached = new Cached<>(t -> new Circular<>(bottom, () -> definition.apply(t)));
		return t -> cached.apply(t).get();
	}

	public static <T, R> Function<T, R> cached(Function<T, R> definition) {
		return new Cached<>(definition);
	}

	public static <T1, T2, R> BiFunction<T1, T2, R> cached(BiFunction<T1, T2, R> definition) {
		Cached<Pair<T1, T2>, R> cached = new Cached<>(pair -> definition.apply(pair.a(), pair.b()));
		return (t1, t2) -> cached.apply(new Pair<>(t1, t2));
	}

	public static <T, R> Function<T, R> identityCached(Function<T, R> definition) {
		Cached<IdentityBox<T>, R> cached = new Cached<>(t -> definition.apply(t.get()));
		return t -> cached.apply(new IdentityBox<>(t));
	}

	public static <T> Predicate<T> predicate(Function<T, Boolean> predicate) {
		return predicate::apply;
	}

}
