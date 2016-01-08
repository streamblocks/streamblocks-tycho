package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.entity.am.Scope;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Variable node that refers to a variable declaration.
 * 
 * For all variables in Actors isScopeVariable() == false.
 * 
 * Static variables are variables declared in a {@link Scope}
 * and {@link #getScopeId()} returns an identifier for the scope where the variable is
 * declared.
 */
public class Variable extends AbstractIRNode {
	private final String name;
	private final int scopeId;
	private final boolean isScopeVariable;

	/**
	 * Constructs a variable.
	 * 
	 * @param name the variable name
	 */
	public static Variable variable(String name) {
		return new Variable(null, name, -1, false);
	}

	/**
	 * Constructs a static scope variable.
	 * 
	 * @param name the variable name
	 * @param scopeId the static scope identifier
	 */
	public static Variable scopeVariable(String name, int scopeId) {
		return new Variable(null, name, scopeId, true);
	}

	public Variable copy(String name) {
		if (Objects.equals(this.name, name) && !this.isScopeVariable) {
			return this;
		}
		return new Variable(this, name, scopeId, false);
	}

	public Variable copy(String name, int scopeId) {
		if (Objects.equals(this.name, name) && this.scopeId == scopeId && this.isScopeVariable) {
			return this;
		}
		return new Variable(this, name, scopeId, true);
	}

	protected Variable(IRNode original, String name, int scopeId, boolean isScopeVariable) {
		super(original);
		this.name = name.intern();
		this.scopeId = scopeId;
		this.isScopeVariable = isScopeVariable;
	}

	/**
	 * Returns true if the variable is static.
	 * 
	 * @return true if the variable is static
	 */
	public boolean isScopeVariable() {
		return isScopeVariable;
	}

	/**
	 * Returns the name of the variable.
	 * 
	 * @return the name of the variable.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the scope identifier if the variable is static.
	 * 
	 * @return the level
	 */
	public int getScopeId() {
		return scopeId;
	}

	public String toString() {
		if (isScopeVariable) {
			return "ScopeVariable(" + name + ", " + scopeId + ")";
		} else {
			return "Variable(" + name + ")";
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public Variable transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return this;
	}
}
