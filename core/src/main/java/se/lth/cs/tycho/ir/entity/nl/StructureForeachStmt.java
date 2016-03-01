package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class StructureForeachStmt extends StructureStatement {

	public StructureForeachStmt(List<GeneratorFilter> generators, List<StructureStatement> statements) {
		this(null, generators, statements);
	}
	private StructureForeachStmt(StructureForeachStmt original, List<GeneratorFilter> generators, List<StructureStatement> statements) {
		super(original);
		this.generators = ImmutableList.from(generators);
		this.statements = ImmutableList.from(statements);
	}

	public StructureForeachStmt copy(List<GeneratorFilter> generators,
			List<StructureStatement> statements) {
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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		generators.forEach(action);
		statements.forEach(action);
		getAttributes().forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				(List) generators.map(transformation),
				(List) statements.map(transformation)
		).withAttributes((List) getAttributes().map(transformation));
	}
}
