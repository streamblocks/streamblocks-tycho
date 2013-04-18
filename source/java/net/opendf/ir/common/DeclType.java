package net.opendf.ir.common;

import java.util.Objects;


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
		this(null, name, namespace);
	}
	
	private DeclType(DeclType original, String name, NamespaceDecl namespace) {
		super(original, name, namespace);
	}
	
	public DeclType copy(String name, NamespaceDecl namespace) {
		if (Objects.equals(getName(), name) && Objects.equals(getNamespaceDecl(), namespace)) {
			return this;
		}
		return new DeclType(this, name, namespace);
	}
}
