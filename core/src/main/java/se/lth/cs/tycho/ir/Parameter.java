package se.lth.cs.tycho.ir;

import java.util.function.Consumer;

public interface Parameter<T extends IRNode, P extends Parameter<T, P>> extends IRNode {
	String getName();

	default P withName(String name) {
		return copy(name, getValue());
	}

	T getValue();

	default P withValue(T value) {
		return copy(getName(), value);
	}

	P copy(String name, T value);

	P clone();

	default P deepClone() {
		return (P) IRNode.super.deepClone();
	}

	@Override
	default void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getValue());
	}

	@Override
	default IRNode transformChildren(Transformation transformation) {
		return copy(getName(), (T) transformation.apply(getValue()));
	}
}
