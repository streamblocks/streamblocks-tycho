package net.opendf.ir.net.ast;


/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public interface StructureStmtVisitor<R, P> {
	public R visitStructureConnectionStmt(StructureConnectionStmt stmt, P p);
	public R visitStructureIfStmt(StructureIfStmt stmt, P p);
	public R visitStructureForeachStmt(StructureForeachStmt stmt, P p);
}