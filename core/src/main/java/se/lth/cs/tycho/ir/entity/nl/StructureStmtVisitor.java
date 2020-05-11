package se.lth.cs.tycho.ir.entity.nl;

public interface StructureStmtVisitor<R, P> {
    public R visitStructureConnectionStmt(StructureConnectionStmt stmt, P p);

    public R visitStructureIfStmt(StructureIfStmt stmt, P p);

    public R visitStructureForeachStmt(StructureForeachStmt stmt, P p);
}
