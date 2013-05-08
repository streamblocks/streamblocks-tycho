package net.opendf.ir.common;

import java.util.ArrayList;
import java.util.List;

import net.opendf.ir.AbstractIRNode;

/**
 * Namespaces impose a hierarchical structure onto the global {@link Decl declarations}. Except for the singular top-level namespace,
 * namespaces are contained within a parent, and each namespace can contain any number of subnamespaces, which all have to have distinct 
 * <it>relative names</it>.
 * 
 * The full name of a namespace is a sequence of names which is its relative name appended to the full name of its parent. The relative 
 * name of the top level namespace is <tt>null</tt>, and its full name is the empty sequence.
 * 
 * Namespaces contain {@link NamespaceDecl namespace declarations}, which in turn contain the actual declarations that are contributed to
 * the namespace. This indirection exists so that local accessibility of a declaration can be properly implemented.
 * 
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Namespace extends AbstractIRNode {
	
	public Namespace  getParent() {
		return parent;
	}
	
	public String  getRelativeName() {
		return relativeName;
	}
	
	public String []  getFullName() {
		if (parent == null) {
			return new String[0];
		} else {
			String [] nm = parent.getFullName();
			String [] fullName = new String [nm.length + 1];
			for (int i = 0; i < nm.length; i++) {
				fullName[i] = nm[i];
			}
			fullName[nm.length] = relativeName;
			return fullName;
		}
	}
	
	public NamespaceDecl  createNamespaceDecl() {
		NamespaceDecl nd = new NamespaceDecl(this);
		return nd;
	}
	
	public List<NamespaceDecl>  getDecls() {
		return decls;
	}
	
	public List<Namespace>  getSubnamespaces() {
		return subnamespaces;
	}
	
	public Namespace  getSubnamespace(String name) {
		for (Namespace n : subnamespaces) {
			if (name.equals(n.relativeName))
				return n;
		}
		return null;
	}
	
	//

	void  addDecl(NamespaceDecl decl) { 
		decls.add(decl);
	}
	
	void  addSubnamespace(Namespace sub) {
		
		assert getSubnamespace(sub.getRelativeName()) == null;
		
		subnamespaces.add(sub);
	}
	
	//
	//  Ctor
	//
	
	public Namespace(Namespace parent, String relativeName) {
		super(null);
		this.parent = parent;
		if (parent != null)
			parent.addSubnamespace(this);
		this.relativeName = relativeName;
		decls = new ArrayList<NamespaceDecl>();
	}
		
	private Namespace 				parent;
	private String 					relativeName;
	private List<NamespaceDecl>		decls;
	private List<Namespace>			subnamespaces;
	
		
	public static Namespace createTopLevelNamespace() {
		return new Namespace(null, null);
	}

}
