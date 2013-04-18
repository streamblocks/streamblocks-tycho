package net.opendf.ir.common;

import java.util.Objects;

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
		this(null, name, null);
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
		this(null, name, type);
	}
	
	private PortDecl(PortDecl original, String name, TypeExpr type) {
		super(original);
		this.name = name;
		this.type = type;
	}
	
	public PortDecl copy(String name, TypeExpr type) {
		if (Objects.equals(this.name, name) && Objects.equals(this.type, type)) {
			return this;
		}
		return new PortDecl(this, name, type);
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
