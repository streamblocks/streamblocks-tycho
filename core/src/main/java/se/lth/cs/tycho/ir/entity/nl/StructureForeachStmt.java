package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class StructureForeachStmt extends AbstractIRNode implements StructureStatement {

	public StructureForeachStmt(Generator generator, List<Expression> filters, List<StructureStatement> statements) {
		this(null, generator, filters, statements);
	}

	private StructureForeachStmt(StructureForeachStmt original, Generator generator, List<Expression> filters, List<StructureStatement> statements) {
		super(original);
		this.generator = generator;
		this.filters = ImmutableList.from(filters);
		this.statements = ImmutableList.from(statements);
	}

	public StructureForeachStmt copy(Generator generator, List<Expression> filters, List<StructureStatement> statements) {
		if (this.generator == generator && Lists.sameElements(this.filters, filters) && Lists.sameElements(this.statements, statements)) {
			return this;
		}
		return new StructureForeachStmt(this, generator, filters, statements);
	}

	public ImmutableList<StructureStatement> getStatements() {
		return statements;
	}

	public Generator getGenerator() {
		return generator;
	}

	public ImmutableList<Expression> getFilters() {
		return filters;
	}

	private final Generator generator;
	private final ImmutableList<Expression> filters;
	private final ImmutableList<StructureStatement> statements;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(generator);
		filters.forEach(action);
		statements.forEach(action);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				transformation.applyChecked(Generator.class, generator),
				transformation.mapChecked(Expression.class, filters),
				transformation.mapChecked(StructureStatement.class, statements)
		);
	}
}
