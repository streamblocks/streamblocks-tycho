package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.pattern.Alternative;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ExprCase extends Expression {

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
