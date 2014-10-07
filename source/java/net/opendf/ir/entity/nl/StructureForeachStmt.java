package net.opendf.ir.entity.nl;

import net.opendf.ir.GeneratorFilter;
import net.opendf.ir.IRNode;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class StructureForeachStmt extends StructureStatement {

	public StructureForeachStmt(ImmutableList<GeneratorFilter> generators, ImmutableList<StructureStatement> statements) {
		this(null, generators, statements);
	}

	public StructureForeachStmt(IRNode original, ImmutableList<GeneratorFilter> generators,
			ImmutableList<StructureStatement> statements) {
		//TODO tool attributes
		super(original, null);
		this.generators = ImmutableList.copyOf(generators);
		this.statements = ImmutableList.copyOf(statements);
	}

	public StructureForeachStmt copy(ImmutableList<GeneratorFilter> generators,
			ImmutableList<StructureStatement> statements) {
		if (Lists.equals(this.generators, generators) && Lists.equals(this.statements, statements)) {
			return this;
		}
		return new StructureForeachStmt(this, generators, statements);
	}

	public ImmutableList<StructureStatement> getStatements() {
		return statements;
	}

	public ImmutableList<GeneratorFilter> getGenerators() {
		return generators;
	}

	private ImmutableList<StructureStatement> statements;
	private ImmutableList<GeneratorFilter> generators;

	@Override
	public <R, P> R accept(StructureStmtVisitor<R, P> v, P p) {
		return v.visitStructureForeachStmt(this, p);
	}

}
