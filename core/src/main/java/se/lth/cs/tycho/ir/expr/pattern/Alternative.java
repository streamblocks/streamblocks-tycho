package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Alternative extends AbstractIRNode {

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
