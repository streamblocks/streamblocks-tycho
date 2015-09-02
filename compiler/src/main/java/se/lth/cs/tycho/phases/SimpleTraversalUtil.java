package se.lth.cs.tycho.phases;

import java.util.function.BiFunction;

public class SimpleTraversalUtil {
	public static <T, P> void visitAll(BiFunction<T, P, Void> visitor, Iterable<T> nodes, P param) {
		for (T node : nodes) {
			visitor.apply(node, param);
		}
	}
}
