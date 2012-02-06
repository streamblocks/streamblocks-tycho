package net.opendf.ir.common;

import net.opendf.ir.common.Decl.DeclKind;

/**
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */
public class DeclType extends Decl {
	

	@Override
	public DeclKind getKind() {
		return DeclKind.type;
	}
	
	@Override
	public void  accept(DeclVisitor v) {
		v.visitDeclType(this);
	}

	public DeclType(String name, NamespaceDecl namespace) {
		super(name, namespace);
	}
}
