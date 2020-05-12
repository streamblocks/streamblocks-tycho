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

	private Expression scrutinee;
	private ImmutableList<Alternative> alternatives;

	public ExprCase(Expression scrutinee, List<Alternative> alternatives) {
		this(null, scrutinee, alternatives);
	}

	private ExprCase(IRNode original, Expression scrutinee, List<Alternative> alternatives) {
		super(original);
		this.scrutinee = scrutinee;
		this.alternatives = ImmutableList.from(alternatives);
	}

	public Expression getScrutinee() {
		return scrutinee;
	}

	public ImmutableList<Alternative> getAlternatives() {
		return alternatives;
	}

	public ExprCase copy(Expression scrutinee, List<Alternative> alternatives) {
		if (Objects.equals(getScrutinee(), scrutinee) && Lists.sameElements(getAlternatives(), alternatives)) {
			return this;
		} else {
			return new ExprCase(this, scrutinee, alternatives);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getScrutinee());
		getAlternatives().forEach(action);
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy((Expression) transformation.apply(getScrutinee()), transformation.mapChecked(Alternative.class, getAlternatives()));
	}
}
