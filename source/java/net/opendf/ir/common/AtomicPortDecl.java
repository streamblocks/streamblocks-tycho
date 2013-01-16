package net.opendf.ir.common;

import java.util.Collections;
import java.util.List;


/**
 * An AtomicPort is a port that contains no further children, and is directly used to communicate data tokens. It can contain a
 * non-null {@link TypeExpr}, which identifies the type of the tokens passing across this port.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class AtomicPortDecl extends PortDecl {

	@Override
	public PortKind getKind() {
		return PortKind.atomic;
	}
	
	@Override
	public List<PortDecl> getChildren() {
		List<PortDecl> p = Collections.emptyList();
		return p;
	}
	
	@Override
	public int  flatWidth() {
		return 1;
	}

	public TypeExpr  getType() { return type; }
	
	//
	// Ctor
	//
	
	
	public AtomicPortDecl(String name, TypeExpr type) {
		this(null, name, type);
	}
	
	public AtomicPortDecl(PortDecl parent, String name, TypeExpr type) {
		super(parent, name);
		this.type = type;
	}
	

	private TypeExpr type;
}
