package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternExpression extends Pattern {

	private Expression expression;

	public PatternExpression(Expression expression) {
		this(null, expression);
	}

	public PatternExpression(IRNode original, Expression expression) {
		super(original);
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	public PatternExpression copy(Expression expression) {
		if (Objects.equals(getExpression(), expression)) {
			return this;
		} else {
			return new PatternExpression(this, expression);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getExpression());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy((Expression) transformation.apply(getExpression()));
	}
}
