package net.opendf.ir.common.decl;


public interface LocalDecl extends Decl {
	public <R, P> R accept(LocalDeclVisitor<R, P> visitor, P param);
}
