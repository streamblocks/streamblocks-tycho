package net.opendf.ir.common;

import java.util.List;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

/**
 * PortDecls (or ports for short) are arranged tree-like in a composite pattern: a port is either atomic (see {@link AtomicPortDecl}) or composite (see {@link CompositePortDecl}), and 
 * composite ports can contain any number of ports as children. Each port has a <it>local name</it>, which is a simple string identifying it
 * relative to its parent port, if it has one. It also has a <it>full name</it>, which is an array of strings identifying the port in the
 * manner of a path from the topmost parent downward.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public abstract class PortDecl extends AbstractIRNode {
	
	public enum PortKind { atomic, composite };

	/**
	 * Ports return the kind of port they are, as an alternative to instanceof which can be used in switch-statements.
	 * 
	 * @return The kind of port, either {@link PortKind#atomic} or {@link PortKind#composite}.
	 */
	
	public abstract PortKind getKind();
	
	/**
	 * 
	 * @return The local name of the port.
	 */
	
	public String  getLocalName() { return name; }
	
	/**
	 * 
	 * @return The full name of the port, as an array of local names relative to the topmost port.
	 */
	
	public String [] getFullName() {
		if (fullName != null)
			return fullName;
		
		if (parent == null) {
			assert name == null;
			fullName = new String [] {};
		} else {
			String [] parentName = parent.getFullName();
			fullName = new String [parentName.length + 1];
			for (int i = 0; i < parentName.length; i++)
				fullName[i] = parentName[i];
			fullName[parentName.length] = name;
		}
		return fullName;
	}

	public PortDecl  getParent() { return parent; }
	
	public void  setParent(PortDecl p) {
		this.parent = p;
		if (p != null)
			this.setContainer(p.getContainer());
	}

	
	abstract public List<PortDecl>  getChildren();

	/**
	 * Every port is uniquely assigned to a {@link PortContainer port container}, which is either a {@link Network} or a {@link Node}.
	 * It can also be null, usually during construction of the port declarations.
	 * 
	 * @return The PortContainer of this port.
	 */
	
	public PortContainer  getContainer() { return container; }	

	/**
	 * Sets the port container of this port, and all its children.
	 * 
	 * @param c The new port container.
	 */

	public void  setContainer(PortContainer c) {
		this.container = c;
		for (PortDecl p : getChildren()) {
			p.setContainer(c);
		}
	}
	
	/**
	 * Locates a port starting from this port using its relative name, i.e. the trailing part of the full name that follows 
	 * the full name of this port.
	 * 
	 * @param relativeName The relative name of the port.
	 * @return The port identified by that name.
	 */
	
	public PortDecl getPort(String [] relativeName) {
		return getPort(0, relativeName);
	}
	
	protected PortDecl getPort(int i, String [] name) {
		if (i >= name.length)
			return this;
		
		for (PortDecl p : getChildren()) {
			if (p.getLocalName().equals(name[i]))
				return p.getPort(i + 1, name);
		}
		throw new RuntimeException("Cannot find port '" + concatName(name) + "'.");
	}
	
	/**
	 * The local width is the number of direct children of this port.
	 * 
	 * @return The number of children of this port.
	 */
	
	public int  localWidth() {
		return getChildren().size();
	}
	
	/**
	 * The flat width is the total number of atomic ports directly orindirectly contained by this port.
	 * 
	 * @return The flat width.
	 */

	public int  flatWidth() {
		int n = 0;
		
		for (PortDecl p :getChildren()) {
			n += p.flatWidth();
		}
		return n;
	}

	//
	// Ctor
	//
	
	/**
	 * The container must be non-null, while the parent and name must either both be null or both be non-null.
	 * 
	 * @param parent The parent port, null for the top-level port.
	 * @param name The port name, null for the top-level port.
	 */

	public PortDecl(PortDecl parent, String name) {
		
		// this do not hold when the tree is built bottom up, which is the case for all LALR parsers.
		//assert (name == null && parent == null) || (name != null && parent != null);
		
		this.parent = parent;
		this.name = name;		
	}	
	
	private String concatName(String [] fullName) {
		StringBuffer sb = new StringBuffer();
		for (String s : fullName) {
			sb.append(portNameSeparator);
			sb.append(s);
		}
		return sb.toString();
	}
	
	public final static String portNameSeparator = ":";
	
	private PortContainer container = null;
	
	private PortDecl parent;
	
	private String name;
	
	private String [] fullName = null;
}
