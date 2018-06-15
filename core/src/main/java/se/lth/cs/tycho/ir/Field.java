package se.lth.cs.tycho.ir;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This class represents a field access of a record.
 */
public class Field extends AbstractIRNode {
	private String name;

	/**
	 * Constructs a field access node.
	 * @param name field name
	 */
	public Field(String name) {
		this(null, name);
	}

	/**
	 * Constructs an updated field access node from a previous one.
	 *
	 * @param original the previous field node
	 * @param name the name of the field
	 */
	private Field(Field original, String name) {
		super(original);
		this.name = name;
	}

	/**
	 * Constructs an updated field access node from a previous one.
	 * Returns this if {@code name} is equal to {@code getName()}.
	 *
	 * @param name the name of the field
	 */
	public Field copy(String name) {
		if (Objects.equals(this.name, name)) {
			return this;
		}
		return new Field(this, name);
	}

	public String getName() {
		return name;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public Field transformChildren(Transformation transformation) {
		return this;
	}
}
