package net.opendf.ir.common;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;

/**
 * Variable node that refers to a variable either by name or by location. The
 * location is either a location of a static or a dynamic variable.
 * 
 * The offset is the number of declarations between the first declaration in the
 * scope where the variable is declared and the declaration of the referenced
 * variable. The first declared variable in a scope has offset 0.
 * 
 * Variables are either static or dynamic. For dynamic variables, the scope is
 * the nesting distance to the lexical scope where the variable is declared.
 * Variables declared in the current scope are at level 0 and variables declared
 * in the immediate enclosing scope are at level 1. For static variables, the
 * scope is the identifier of the scope where it is declared.
 */
public class Variable extends AbstractIRNode {
	private final String name;
	private final int scope;
	private final int offset;
	private final boolean dynamic;

	/**
	 * Constructs a variable with a name.
	 * 
	 * @param name
	 *            the variable name
	 */
	public static Variable namedVariable(String name) {
		return new Variable(null, name, -1, -1, false);
	}

	/**
	 * Constructs a static variable with a name, scope and offset.
	 * 
	 * @param name
	 *            the variable name
	 * @param scope
	 *            the static scope number
	 * @param offset
	 *            the offset
	 */
	public static Variable staticVariable(String name, int scope, int offset) {
		assert scope >= 0;
		assert offset >= 0;
		return new Variable(null, name, scope, offset, false);
	}

	/**
	 * Constructs a dynamic variable with a name, scope and offset.
	 * 
	 * @param name
	 *            the variable name
	 * @param scope
	 *            the lexical scope distance
	 * @param offset
	 *            the offset
	 */
	public static Variable dynamicVariable(String name, int scope, int offset) {
		assert scope >= 0;
		assert offset >= 0;
		return new Variable(null, name, scope, offset, true);
	}

	public Variable copy(String name, int scope, int offset, boolean dynamic) {
		if (Objects.equals(this.name, name) && this.scope == scope && this.offset == offset && this.dynamic == dynamic) {
			return this;
		}
		return new Variable(this, name, scope, offset, dynamic);
	}

	private Variable(Variable original, String name, int scope, int offset, boolean dynamic) {
		super(original);
		this.name = name.intern();
		this.scope = scope;
		this.offset = offset;
		this.dynamic = dynamic;
	}

	/**
	 * Returns true if the variable is static and false if it does not have a
	 * location or the location is to a dynamic variable.
	 * 
	 * @return true if the variable is static
	 */
	public boolean isStatic() {
		return hasLocation() && !dynamic;
	}

	/**
	 * Returns true if the variable is dynamic and false if it does not have a
	 * location or the location is to a static variable.
	 * 
	 * @return true if the variable is static
	 */
	public boolean isDynamic() {
		return hasLocation() && dynamic;
	}

	/**
	 * Returns true if the variable is specified with a location.
	 * 
	 * @return true if the variable is specified with a location
	 */
	public boolean hasLocation() {
		return scope >= 0 && offset >= 0;
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
	 * Returns the scope of the variable. For dynamic variables, this is the
	 * static nesting distance and for static variables this is that static
	 * scope number.
	 * 
	 * @return the level
	 */
	public int getScope() {
		return scope;
	}

	/**
	 * Returns the offset if the variable is specified with a location.
	 * 
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
}
