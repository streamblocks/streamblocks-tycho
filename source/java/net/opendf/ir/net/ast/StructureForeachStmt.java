package net.opendf.ir.net.ast;

import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.util.ImmutableList;

public class StructureForeachStmt extends StructureStatement {
	public StructureForeachStmt(ImmutableList<GeneratorFilter> generators, ImmutableList<StructureStatement> statements){
		this.generators = ImmutableList.copyOf(generators);
		this.statements = ImmutableList.copyOf(statements);
	}
	public ImmutableList<StructureStatement> getStatements(){
		return statements;
	}
	public ImmutableList<GeneratorFilter> getGenerators(){
		return generators;
	}

	private ImmutableList<StructureStatement> statements;
	private ImmutableList<GeneratorFilter> generators;

	@Override
	public <R, P> R accept(StructureStmtVisitor<R, P> v, P p) {
		return v.visitStructureForeachStmt(this, p);
	}

}
