package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

public class NamespaceDecl extends AbstractIRNode {
	
	public NamespaceDecl  getParent() {
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
	
	public NamespaceDecl(NamespaceDecl parent, String relativeName) {
		this.parent = parent;
		this.relativeName = relativeName;
	}
		
	private NamespaceDecl 	parent;
	private String 		relativeName;
	
	
	
	public static NamespaceDecl createTopLevelNamespace() {
		return new NamespaceDecl(null, null);
	}

}
