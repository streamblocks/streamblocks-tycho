package net.opendf.ir.common;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;

/**
 * Variable node that refers to a variable declaration.
 * 
 * For all variables in Actors isScopeVariable() == false.
 * 
 * Static variables are variables declared in a {@link net.opendf.ir.am.Scope}
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
	 * @param scope the static scope identifier
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
			return "StackVariable(" + name + ")";
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Variable)) {
			return false;
		}
		Variable that = (Variable) o;
		if (this.isScopeVariable != that.isScopeVariable) {
			return false;
		}
		if (isScopeVariable && this.scopeId != that.scopeId) {
			return false;
		}
		if (!Objects.equals(this.name, that.name)) {
			return false;
		}
		return true;
	}
}
