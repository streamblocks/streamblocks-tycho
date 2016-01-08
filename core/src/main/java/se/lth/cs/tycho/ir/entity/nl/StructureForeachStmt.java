package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.Attributable;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;

public class StructureForeachStmt extends StructureStatement {

	public StructureForeachStmt(ImmutableList<GeneratorFilter> generators, ImmutableList<StructureStatement> statements) {
		super(null);
		this.generators = ImmutableList.from(generators);
		this.statements = ImmutableList.from(statements);
	}

	public StructureForeachStmt copy(ImmutableList<GeneratorFilter> generators,
			ImmutableList<StructureStatement> statements) {
		if (Lists.equals(this.generators, generators) && Lists.equals(this.statements, statements)) {
			return this;
		}
		return new StructureForeachStmt(generators, statements);
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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		generators.forEach(action);
		statements.forEach(action);
	}

	@Override
	public Attributable withToolAttributes(ImmutableList<ToolAttribute> attributes) {
		throw new UnsupportedOperationException();
	}
}
