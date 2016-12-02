package se.lth.cs.tycho.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Optionals {
	private Optionals() {}

	public static <T> Stream<T> toStream(Optional<T> optional) {
		return optional.map(Stream::of).orElse(Stream.empty());
	}

	public static <To, From extends To> Optional<To> upCast(Optional<From> optional) {
		return optional.map(Function.identity());
	}

}
