package net.opendf.ir.common;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;

/**
 * Variable node that refers to a variable declaration.
 * 
 * Static variables are variables declared in a {@link net.opendf.ir.am.Scope}
 * and {@link #getScopeId()} returns an identifier for the scope where the variable is
 * declared.
 */
public class Variable extends AbstractIRNode {
	private final String name;
	private final int scopeId;
	private final boolean isStatic;

	/**
	 * Constructs a variable.
	 * 
	 * @param name the variable name
	 */
	public static Variable variable(String name) {
		return new Variable(null, name, -1, false);
	}

	/**
	 * Constructs a static variable.
	 * 
	 * @param name the variable name
	 * @param scope the static scope identifier
	 */
	public static Variable staticVariable(String name, int scope) {
		return new Variable(null, name, scope, true);
	}

	public Variable copy(String name) {
		if (Objects.equals(this.name, name) && !this.isStatic) {
			return this;
		}
		return new Variable(this, name, scopeId, isStatic);
	}

	public Variable copy(String name, int scopeId) {
		if (Objects.equals(this.name, name) && this.scopeId == scopeId && this.isStatic) {
			return this;
		}
		return new Variable(this, name, scopeId, isStatic);
	}

	private Variable(Variable original, String name, int scope, boolean isStatic) {
		super(original);
		this.name = name.intern();
		this.scopeId = scope;
		this.isStatic = isStatic;
	}

	/**
	 * Returns true if the variable is static.
	 * 
	 * @return true if the variable is static
	 */
	public boolean isStaticVariable() {
		return isStatic;
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
		if (isStatic) {
			return "StaticVariable(" + name + ", " + scopeId + ")";
		} else {
			return "Variable(" + name + ")";
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Variable)) {
			return false;
		}
		Variable that = (Variable) o;
		if (this.isStatic != that.isStatic) {
			return false;
		}
		if (isStatic && this.scopeId != that.scopeId) {
			return false;
		}
		if (!Objects.equals(this.name, that.name)) {
			return false;
		}
		return true;
	}
}
