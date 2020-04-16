package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternAlias extends Pattern {

	private Pattern alias;
	private Expression expression;

	public PatternAlias(Pattern alias, Expression expression) {
		this(null, alias, expression);
	}

	public PatternAlias(IRNode original, Pattern alias, Expression expression) {
		super(original);
		this.alias = alias;
		this.expression = expression;
	}

	public PatternAlias copy(Pattern alias, Expression expression) {
		if (Objects.equals(getAlias(), alias) && Objects.equals(getExpression(), expression)) {
			return this;
		} else {
			return new PatternAlias(this, alias, expression);
		}
	}

	public Pattern getAlias() {
		return alias;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getAlias());
		action.accept(getExpression());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(transformation.applyChecked(Pattern.class, getAlias()), transformation.applyChecked(Expression.class, getExpression()));
	}
}
