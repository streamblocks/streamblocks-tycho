package se.lth.cs.tycho.ir.stmt;

import java.util.List;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class StmtForeach extends Statement {

	private final Generator generator;
	private final ImmutableList<Expression> filters;
	private final Statement body;

	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		return v.visitStmtForeach(this, p);
	}

	public StmtForeach(Generator generators, List<Expression> filters, Statement body) {
		this(null, generators, filters, body);
	}

	private StmtForeach(StmtForeach original, Generator generator, List<Expression> filters, Statement body) {
		super(original);
		this.generator = generator;
		this.filters = ImmutableList.from(filters);
		this.body = body;
	}

	public StmtForeach copy(Generator generator, List<Expression> filters, Statement body) {
		if (this.generator == generator && Lists.sameElements(this.filters, filters) && this.body == body) {
			return this;
		} else {
			return new StmtForeach(this, generator, filters, body);
		}
	}

	public Generator getGenerator() {
		return generator;
	}

	public StmtForeach withGenerator(Generator generator) {
		return copy(generator, filters, body);
	}

	public ImmutableList<Expression> getFilters() {
		return filters;
	}

	public StmtForeach withFilters(List<Expression> filters) {
		return copy(generator, filters, body);
	}

	public Statement getBody() {
		return body;
	}

	public StmtForeach withBody(Statement body) {
		return copy(generator, filters, body);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(generator);
		filters.forEach(action);
		action.accept(body);
	}

	@Override
	@SuppressWarnings("unchecked")
	public StmtForeach transformChildren(Transformation transformation) {
		return copy(
				transformation.applyChecked(Generator.class, generator),
				transformation.mapChecked(Expression.class, filters),
				transformation.applyChecked(Statement.class, body));
	}
}
