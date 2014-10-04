package net.opendf.ir.common.decl;

public interface GlobalDeclVisitor<R, P> {
	public R visitGlobalVarDecl(GlobalVarDecl node, P param);
	public R visitGlobalTypeDecl(GlobalTypeDecl node, P param);
	public R visitGlobalEntityDecl(GlobalEntityDecl node, P param);
}
