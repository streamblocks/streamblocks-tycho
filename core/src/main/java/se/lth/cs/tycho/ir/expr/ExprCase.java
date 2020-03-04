package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ExprCase extends Expression {

	public static class Alternative extends AbstractIRNode {

		private Pattern pattern;
		private ImmutableList<Expression> guards;
		private Expression expression;

		public Alternative(Pattern pattern, List<Expression> guards, Expression expression) {
			this(null, pattern, guards, expression);
		}

		private Alternative(IRNode original, Pattern pattern, List<Expression> guards, Expression expression) {
			super(original);
			this.pattern = pattern;
			this.guards = ImmutableList.from(guards);
			this.expression = expression;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public ImmutableList<Expression> getGuards() {
			return guards;
		}

		public Expression getExpression() {
			return expression;
		}

		public Alternative copy(Pattern pattern, List<Expression> guards, Expression expression) {
			if (Objects.equals(getPattern(), pattern) && Lists.sameElements(getGuards(), guards) && Objects.equals(getExpression(), expression)) {
				return this;
			} else {
				return new Alternative(this, pattern, guards, expression);
			}
		}

		@Override
		public void forEachChild(Consumer<? super IRNode> action) {
			action.accept(getPattern());
			getGuards().forEach(action);
			action.accept(getExpression());
		}

		@Override
		public IRNode transformChildren(Transformation transformation) {
			return copy((Pattern) transformation.apply(getPattern()), transformation.mapChecked(Expression.class, getGuards()), (Expression) transformation.apply(getExpression()));
		}
	}

	private Expression expression;
	private ImmutableList<Alternative> alternatives;
	private Expression default_;

	public ExprCase(Expression expression, List<Alternative> alternatives, Expression default_) {
		this(null, expression, alternatives, default_);
	}

	private ExprCase(IRNode original, Expression expression, List<Alternative> alternatives, Expression default_) {
		super(original);
		this.expression = expression;
		this.alternatives = ImmutableList.from(alternatives);
		this.default_ = default_;
	}

	public Expression getExpression() {
		return expression;
	}

	public ImmutableList<Alternative> getAlternatives() {
		return alternatives;
	}

	public Expression getDefault() {
		return default_;
	}

	public ExprCase copy(Expression expression, List<Alternative> alternatives, Expression default_) {
		if (Objects.equals(getExpression(), expression) && Lists.sameElements(getAlternatives(), alternatives) && Objects.equals(getDefault(), default_)) {
			return this;
		} else {
			return new ExprCase(this, expression, alternatives, default_);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getExpression());
		getAlternatives().forEach(action);
		action.accept(getDefault());
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy((Expression) transformation.apply(getExpression()), transformation.mapChecked(Alternative.class, getAlternatives()), (Expression) transformation.apply(getDefault()));
	}
}
