package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

/**
 * Variable node that refers to a variable either by name or by location. The
 * location is either a location of a static or a dynamic variable.
 * 
 * The variable number is the number in the order of declarations in the scope
 * where the variable is declared. The first declared variable in a scope has
 * variable number 0.
 * 
 * The level is the distance to the lexical scope where the variable is
 * declared. Variables declared in the current scope are at level 0 and
 * variables declared in the immediate enclosing scope are at level 1.
 */
public class Variable extends AbstractIRNode {
	private final String name;
	private final int level;
	private final int number;

	/**
	 * Constructs a variable with a name.
	 * 
	 * @param name
	 *            the variable name
	 */
	public Variable(String name) {
		this(name, -1, -1);
	}

	/**
	 * Constructs a static variable with a name and its variable number.
	 * 
	 * @param name
	 *            the variable name
	 * @param number
	 *            the variable number in the static scope
	 */
	public Variable(String name, int number) {
		this(name, -1, number);
	}

	/**
	 * Constructs a dynamic variable with a name, level and variable number.
	 * 
	 * @param name
	 *            the variable name
	 * @param level
	 *            the lexical scope distance
	 * @param number
	 *            the variable number in the scope where it is declared
	 */
	public Variable(String name, int level, int number) {
		this.name = name.intern();
		this.level = level;
		this.number = number;
	}

	/**
	 * Returns true if the variable is static and false if it does not have a
	 * location or the location is to a dynamic variable.
	 * 
	 * @return true if the variable is static
	 */
	public boolean isStatic() {
		return hasLocation() && level < 0;
	}

	/**
	 * Returns true if the variable is dynamic and false if it does not have a
	 * location or the location is to a static variable.
	 * 
	 * @return true if the variable is static
	 */
	public boolean isDynamic() {
		return hasLocation() && level >= 0;
	}

	/**
	 * Returns true if the variable is specified with a location.
	 * 
	 * @return true if the variable is specified with a location
	 */
	public boolean hasLocation() {
		return number >= 0;
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
	 * Returns the level if the variable is dynamic and specified with a
	 * location.
	 * 
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Returns the variable number if the variable is specified with a location.
	 * 
	 * @return the variable number
	 */
	public int getNumber() {
		return number;
	}
}
