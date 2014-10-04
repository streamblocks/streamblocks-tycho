package net.opendf.ir.common.decl;

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

	public DeclType(String name) {
		this(null, name);
	}
	
	private DeclType(DeclType original, String name) {
		super(original, name);
	}
	
	public DeclType copy(String name) {
		if (Objects.equals(getName(), name)) {
			return this;
		}
		return new DeclType(this, name);
	}
}
