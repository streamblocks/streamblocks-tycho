package net.opendf.ir.common;

import java.util.ArrayList;
import java.util.List;


/**
 * A CompositePort is a structure that groups a number of component ports, and allows access to them by name.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class CompositePortDecl extends PortDecl {

	@Override
	public PortKind getKind() {
		return PortKind.composite;
	}

	@Override
	public List<PortDecl> getChildren() {
		return children;
	}
	
	public void addChild(PortDecl p) {
		children.add(p);
		p.setParent(this);
	}
	
	//
	// Ctor
	//
	
	public CompositePortDecl() {
		this (null, null);
	}
	
	public CompositePortDecl(PortDecl parent, String name) {
		super(parent, name);
	}
	
	
	private List<PortDecl>  children = new ArrayList<PortDecl>();

}
