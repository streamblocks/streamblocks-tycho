package se.lth.cs.tycho.ir.entity.nl;


/**
 * @author Per Andersson <Per.Andersson@cs.lth.se>
 *
 */

public interface StructureStmtVisitor<R, P> {
	R visitStructureConnectionStmt(StructureConnectionStmt stmt, P p);
	R visitStructureIfStmt(StructureIfStmt stmt, P p);
	R visitStructureForeachStmt(StructureForeachStmt stmt, P p);
}