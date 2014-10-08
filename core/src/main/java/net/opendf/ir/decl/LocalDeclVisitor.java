package net.opendf.ir.decl;

public interface LocalDeclVisitor<R, P> {
	public R visitLocalVarDecl(LocalVarDecl node, P param);
	public R visitLocalTypeDecl(LocalTypeDecl node, P param);
}
