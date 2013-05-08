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
 * The level is the distance to the lexical scope where the variable is
 * declared. Variables declared in the current scope are at level 0 and
 * variables declared in the immediate enclosing scope are at level 1.
 */
public class Variable extends AbstractIRNode {
	private final String name;
	private final int level;
	private final int offset;

	/**
	 * Constructs a variable with a name.
	 * 
	 * @param name
	 *            the variable name
	 */
	public Variable(String name) {
		this(null, name, -1, -1);
	}

	public Variable copy(String name) {
		return copy(name, -1, -1);
	}

	/**
	 * Constructs a static variable with a name and its offset.
	 * 
	 * @param name
	 *            the variable name
	 * @param offset
	 *            the offset
	 */
	public Variable(String name, int offset) {
		this(null, name, -1, offset);
		assert offset >= 0;
	}
	
	public Variable copy(String name, int offset) {
		return copy(name, -1, offset);
	}

	/**
	 * Constructs a dynamic variable with a name, level and offset.
	 * 
	 * @param name
	 *            the variable name
	 * @param level
	 *            the lexical scope distance
	 * @param offset
	 *            the offset
	 */
	public Variable(String name, int level, int offset) {
		this(null, name, level, offset);
		assert level >= 0;
		assert offset >= 0;
	}
	
	public Variable copy(String name, int level, int offset) {
		if (Objects.equals(this.name, name) && this.level == level && this.offset == offset) {
			return this;
		}
		return new Variable(this, name, level, offset);
	}
	
	private Variable(Variable original, String name, int level, int offset) {
		super(original);
		this.name = name.intern();
		this.level = level;
		this.offset = offset;
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
		return offset >= 0;
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
	 * Returns the offset if the variable is specified with a location.
	 * 
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
}
