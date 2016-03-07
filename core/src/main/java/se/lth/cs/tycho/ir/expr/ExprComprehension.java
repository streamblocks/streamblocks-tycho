package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class ExprComprehension extends Expression {
	private final Generator generator;
	private final ImmutableList<Expression> filters;
	private final Expression collection;

	public ExprComprehension(Generator generator, List<Expression> filters, Expression collection) {
		this(null, generator, filters, collection);
	}

	private ExprComprehension(IRNode original, Generator generator, List<Expression> filters, Expression collection) {
		super(original);
		this.generator = generator;
		this.filters = ImmutableList.from(filters);
		this.collection = collection;
	}

	public ExprComprehension copy(Generator generator, List<Expression> filters, Expression collection) {
		if (this.generator == generator && Lists.sameElements(this.filters, filters) && this.collection == collection) {
			return this;
		} else {
			return new ExprComprehension(this, generator, filters, collection);
		}
	}

	public Generator getGenerator() {
		return generator;
	}

	public ExprComprehension withGenerator(Generator generator) {
		return copy(generator, filters, collection);
	}

	public ImmutableList<Expression> getFilters() {
		return filters;
	}

	public ExprComprehension withFilters(List<Expression> filters) {
		return copy(generator, filters, collection);
	}

	public Expression getCollection() {
		return collection;
	}

	public ExprComprehension withCollection(Expression collection) {
		return copy(generator, filters, collection);
	}

	@Override
	public ExprComprehension deepClone() {
		return (ExprComprehension) super.deepClone();
	}

	@Override
	public ExprComprehension clone() {
		return (ExprComprehension) super.clone();
	}

	@Override
	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(generator);
		filters.forEach(action);
		action.accept(collection);
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				transformation.applyChecked(Generator.class, generator),
				transformation.mapChecked(Expression.class, filters),
				transformation.applyChecked(Expression.class, collection)
		);
	}
}
