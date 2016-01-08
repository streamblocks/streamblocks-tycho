package se.lth.cs.tycho.ir;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;


public class Variable extends AbstractIRNode {
	private final String originalName;
	private final String name;

	/**
	 * Constructs a variable.
	 * 
	 * @param name the variable name
	 */
	public static Variable variable(String name) {
		return new Variable(null, name);
	}

	public Variable withName(String name) {
		if (Objects.equals(this.name, name)) {
			return this;
		}
		return new Variable(this, name);
	}

	private Variable(Variable original, String name) {
		super(original);
		this.originalName = original == null ? name : original.name;
		this.name = name;
	}

	/**
	 * Returns the name of the variable.
	 * 
	 * @return the name of the variable.
	 */
	public String getName() {
		return name;
	}

	public String getOriginalName() {
		return originalName;
	}

	public String toString() {
		return "Variable(" + name + ")";
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public Variable transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return this;
	}
}
