package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

/**
 * Port declaration.
 */
public class PortDecl extends AbstractIRNode {
	private String name;
	private TypeExpr type;

	/**
	 * Constructs a port with a name.
	 * 
	 * @param name
	 *            the port name
	 */
	public PortDecl(String name) {
		this(name, null);
	}

	/**
	 * Constructs a port with a name and a type.
	 * 
	 * @param name
	 *            the port name
	 * @param type
	 *            the type of the tokens
	 */
	public PortDecl(String name, TypeExpr type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns the name of the port.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type of the tokens on port.
	 * 
	 * @return the type of the tokens
	 */
	public TypeExpr getType() {
		return type;
	}

}
