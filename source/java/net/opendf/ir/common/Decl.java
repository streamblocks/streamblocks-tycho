/* 
BEGINCOPYRIGHT JWJ
ENDCOPYRIGHT
*/

package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

/**
 * Declarations bind a name to a an object in a way that code may refer to the object by that name. They are distinguished in
 * a number of ways: <ol type="a">
 * <li>the location of their occurrence (top-level inside a {@link NamespaceDecl namespace declaration} or scoped within other program code),
 * <li>whether they directly declare the name, or do so by reference to another global declaration (import),
 * <li>in case of top-level declarations, their accessibility (local, private, or public),
 * <li>the kind of object they declare (variable, type, or entity).
 * </ol>
 * The name inside a declaration is interned, so it can be compared for identity more efficiently.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

abstract public class Decl extends AbstractIRNode {
	
	public static enum DeclKind { value, type, entity};
	
	public static enum Availability { aScope, aLocal, aPrivate, aPublic };
	

	abstract public DeclKind  getKind();
	
	abstract public <R,P> R accept(DeclVisitor<R,P> v, P p);
	
	public <R> R accept(DeclVisitor<R,Void> v) {
		return accept(v, null);
	}
	
	
	public String getName() { return name; }
	
	public NamespaceDecl getNamespaceDecl() { return namespaceDecl; }

	public boolean isImport() { return isImport; }
	

	
	public String [] getQID() { 

		assert isImport;
		
		return qid;
	}
		
	public String getOriginalName() {

		assert isImport;
		
		return qid[qid.length - 1];
	}
	
	//
	// Ctor
	//
	

	public Decl(String name, NamespaceDecl namespaceDecl) {
		isImport = false;
		this.name = name.intern();
		this.namespaceDecl = namespaceDecl;
		if (namespaceDecl != null)
			namespaceDecl.addDecl(this);
	}
	
	public Decl(String name, NamespaceDecl namespaceDecl, String [] qid) {
		
		assert qid != null && qid.length >= 1;
		
		isImport = true;
		this.name = name.intern();
		this.namespaceDecl = namespaceDecl;
		if (namespaceDecl != null)
			namespaceDecl.addDecl(this);
		this.qid = qid;
	}
	
	

	
	
	private String   name;
	private boolean  isImport;
	private NamespaceDecl namespaceDecl;
	
	//  import
	
	private String []  qid;

}
