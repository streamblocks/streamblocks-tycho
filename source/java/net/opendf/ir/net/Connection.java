package net.opendf.ir.net;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.PortDecl;

/**
 * A Connection links two ports.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Connection extends AbstractIRNode {
	
	public PortDecl  A() { return a; }
	
	public PortDecl  B() { return b; }
	
	//
	//  Ctor
	// 
	
	public Connection(PortDecl a, PortDecl b) {
		super(null);
		this.a = a;
		this.b = b;
	}
	
	private PortDecl a, b;
}
