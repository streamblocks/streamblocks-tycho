package net.opendf.ir.common;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;

/**
 * A Port node that refers to a port either by name or by location. The location
 * is defined as the offset into the list of port declarations.
 */
public class Port extends AbstractIRNode {
	private String name;
	private int offset;

	/**
	 * Constructs a Port with a name.
	 * 
	 * @param name
	 *            the name
	 */
	public Port(String name) {
		this(null, name, -1);
	}

	/**
	 * Constructs a Port with a name and a location.
	 * 
	 * @param name
	 *            the name
	 * @param offset
	 *            the offset into the list of port declarations.
	 */
	public Port(String name, int offset) {
		this(null, name, offset);
		assert offset >= 0;
	}

	private Port(Port original, String name, int offset) {
		super(original);
		this.name = name;
		this.offset = offset;
	}
	
	public Port copy(String name) {
		return copy(name, -1);
	}
	
	public Port copy(String name, int offset) {
		if (Objects.equals(this.name, name) && this.offset == offset) {
			return this;
		}
		return new Port(this, name, offset);
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
	 * Returns true if the port is referred to by offset into the list of port
	 * declarations.
	 * 
	 * @return true if the port has a location
	 */
	public boolean hasLocation() {
		return offset >= 0;
	}

	/**
	 * Returns the offset into the list of port declarations if the port has a
	 * location.
	 * 
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + offset >= 0 ? offset + 1 : 0;
		return result;
	}

	/**
	 * Ports with a location are equal if both the names and the locations are
	 * equal. Ports without location are equal if both names are equal. Ports
	 * without location is never equal to a port with a location.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Port) {
			Port that = (Port) o;
			if (this.getName() != that.getName())
				return false;
			if (this.hasLocation() != that.hasLocation())
				return false;
			if (this.hasLocation() && this.getOffset() != that.getOffset())
				return false;
			return true;
		} else {
			return false;
		}
	}
	

}
