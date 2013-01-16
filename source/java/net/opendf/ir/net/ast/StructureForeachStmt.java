package net.opendf.ir.net.ast;

import net.opendf.ir.common.GeneratorFilter;

public class StructureForeachStmt extends StructureStatement {
	public StructureForeachStmt(GeneratorFilter[] generators, StructureStatement[] statements){
		this.generators = generators;
		this.statements = statements;
	}
	public StructureStatement[] getStatements(){
		return statements;
	}
	public GeneratorFilter[] getGenerators(){
		return generators;
	}

	private StructureStatement[] statements;
	private GeneratorFilter[] generators;

	@Override
	public <R, P> R accept(StructureStmtVisitor<R, P> v, P p) {
		return v.visitStructureForeachStmt(this, p);
	}

}
