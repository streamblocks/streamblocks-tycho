package net.opendf.ir.common;

import java.util.ArrayList;
import java.util.List;

import net.opendf.ir.AbstractIRNode;

/**
 * A namespace declaration represents a lexical entity that contributes {@link Decl declarations} to a namespace, and which also serves as the 
 * scope of any of its declaration with 'local' accessibility.
 * 
 * In addition to any number of (variable, type, and entity) declarations, it may also contain other namespace declarations.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class NamespaceDecl extends AbstractIRNode {

	
	public List<Decl>  getDecls() {
		return decls;
	}
	
	public List<NamespaceDecl>  getSubnamespaceDecls() {
		return subdecls;
	}
	
	public Namespace  getNamespace() {
		return namespace;
	}
	
	
	//
	
	void  addDecl(Decl d) {
		decls.add(d);
	}
	
	void  addSubnamespaceDecl(NamespaceDecl nd) {
		subdecls.add(nd);
	}
	
	//
	//  Ctor
	//
	
	public NamespaceDecl(Namespace namespace) {
		super(null);
		this.namespace = namespace;
		namespace.addDecl(this);
		decls = new ArrayList<Decl>();
	}
	

	private Namespace   		namespace;
	private List<Decl>			decls;
	private List<NamespaceDecl>	subdecls;
	
}
