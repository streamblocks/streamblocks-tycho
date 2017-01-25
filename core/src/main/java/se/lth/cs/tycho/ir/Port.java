package se.lth.cs.tycho.ir;

import java.util.function.Consumer;

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
	
	public Port withName(String name) {
		if (this.name.equals(name)) {
			return this;
		} else {
			return new Port(this, name);
		}
	}
	
	/**
	 * Returns the name of the port.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
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
	public Port transformChildren(Transformation transformation) {
		return this;
	}
}
