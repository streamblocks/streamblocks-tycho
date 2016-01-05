package se.lth.cs.tycho.ir;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Port node that refers to a port either by name or by location. The location
 * is defined as the offset into the list of port declarations.
 */
public class Port extends AbstractIRNode {
	private final String name;

	/**
	 * Constructs a Port with a name.
	 * 
	 * @param name
	 *            the name
	 */
	public Port(String name) {
		this(null, name);
	}

	private Port(Port original, String name) {
		super(original);
		this.name = name;
	}
	
	public Port copy(String name) {
		if (this.name.equals(name)) {
			return this;
		} else {
			return new Port(this, name);
		}
	}
	
	public Port copy(String name, int offset) {
		throw new RuntimeException();
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
		return false;
	}

	/**
	 * Returns the offset into the list of port declarations if the port has a
	 * location.
	 * 
	 * @return the offset
	 */
	public int getOffset() {
		return -1;
	}

	@Override
	public Port clone() {
		return (Port) super.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Port port = (Port) o;

		return !(name != null ? !name.equals(port.name) : port.name != null);

	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public Port transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return this;
	}
}
