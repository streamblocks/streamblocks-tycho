package net.opendf.ir.common;


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
	public <R,P> R accept(DeclVisitor<R,P> v, P p) {
		return v.visitDeclType(this, p);
	}

	public DeclType(String name, NamespaceDecl namespace) {
		super(name, namespace);
	}
}
