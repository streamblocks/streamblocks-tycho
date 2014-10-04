package net.opendf.ir.common.decl;

public interface ParDeclVisitor<R, P> {
	public R visitParDeclValue(ParDeclValue node, P param);
	public R visitParDeclType(ParDeclType node, P param);
}
