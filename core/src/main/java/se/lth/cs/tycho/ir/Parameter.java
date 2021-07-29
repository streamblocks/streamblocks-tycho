package se.lth.cs.tycho.ir;

import java.util.function.Consumer;

/**
 * Base type for parameter assignment nodes.
 *
 * @param <T> the type of value this parameter refers to.
 * @param <P> the implementation class.
 */
public interface Parameter<T extends IRNode, P extends Parameter<T, P>> extends IRNode {
	/**
	 * Returns the parameter name to assign.
	 * @return the parameter name
	 */
	String getName();

	/**
	 * Returns a parameter node where the name is replace by {@code name}.
	 * @param name the new name
	 * @return a new parameter node
	 */
	default P withName(String name) {
		return copy(name, getValue());
	}

	/**
	 * Returns the value that is assigned.
	 * @return the value
	 */
	T getValue();

	/**
	 * Returns a parameter node where the value is replaced by {@code value}.
	 * @param value the new value
	 * @return a new parameter node
	 */
	default P withValue(T value) {
		return copy(getName(), value);
	}

	/**
	 * Returns a parameter with the given name and value, and returns
	 * {@code this} if the given {@code name} and {@code value} are the same
	 * as the current.
	 *
	 * @param name the new name
	 * @param value the new value
	 * @return a parameter with the given name and value.
	 */
	P copy(String name, T value);

	/**
	 * Returns a clone of this parameter node.
	 *
	 * @return a clone
	 */
	P clone();

	/**
	 * Returns a clone of the tree rooted in this node.
	 *
	 * @return a clone
	 */
	default P deepClone() {
		return (P) IRNode.super.deepClone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void forEachChild(Consumer<? super IRNode> action) {
		if (getValue() != null)
			action.accept(getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IRNode transformChildren(Transformation transformation) {
		if (getValue() != null)
			return copy(getName(), (T) transformation.apply(getValue()));
		else
			return copy(getName(), null);
	}
}
