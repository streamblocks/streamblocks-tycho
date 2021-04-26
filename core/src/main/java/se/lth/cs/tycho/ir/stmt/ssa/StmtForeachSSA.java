package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class StmtForeachSSA extends Statement {

	private final Generator generator;
	private final ImmutableList<Expression> filters;
	private final ImmutableList<Statement> body;
	private final ImmutableList<Statement> header;

	public StmtForeachSSA(Generator generators, List<Expression> filters, List<Statement> body, List<Statement> header) {
		this(null, generators, filters, body, header);
	}

	private StmtForeachSSA(StmtForeachSSA original, Generator generator, List<Expression> filters, List<Statement> body, List<Statement> header) {
		super(original);
		this.generator = generator;
		this.filters = ImmutableList.from(filters);
		this.body = ImmutableList.from(body);
		this.header = ImmutableList.from(header);
	}

	public StmtForeachSSA copy(Generator generator, List<Expression> filters, List<Statement> body, List<Statement> header) {
		if (this.generator == generator && Lists.sameElements(this.filters, filters) && Lists.sameElements(this.body, body)) {
			return this;
		} else {
			return new StmtForeachSSA(this, generator, filters, body, header);
		}
	}

	public Generator getGenerator() {
		return generator;
	}

	public StmtForeachSSA withGenerator(Generator generator) {
		return copy(generator, filters, body, header);
	}

	public ImmutableList<Expression> getFilters() {
		return filters;
	}

	public StmtForeachSSA withFilters(List<Expression> filters) {
		return copy(generator, filters, body, header);
	}

	public ImmutableList<Statement> getBody() {
		return body;
	}

	public StmtForeachSSA withBody(List<Statement> body) {
		return copy(generator, filters, body, header);
	}

	public ImmutableList<Statement> getHeader() {
		return header;
	}

	public StmtForeachSSA withHeader(List<Statement> header) {
		return copy(generator, filters, body, header);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(generator);
		filters.forEach(action);
		body.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public StmtForeachSSA transformChildren(Transformation transformation) {
		return copy(
				transformation.applyChecked(Generator.class, generator),
				transformation.mapChecked(Expression.class, filters),
				transformation.mapChecked(Statement.class, body),
				transformation.mapChecked(Statement.class, header));
	}
}
