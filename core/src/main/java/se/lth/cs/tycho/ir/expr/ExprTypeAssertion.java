package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.Objects;
import java.util.function.Consumer;

public class ExprTypeAssertion extends Expression {

	private Expression expression;
	private TypeExpr type;

	public ExprTypeAssertion(Expression expression, TypeExpr type) {
		this(null, expression, type);
	}

	private ExprTypeAssertion(IRNode original, Expression expression, TypeExpr type) {
		super(original);
		this.expression = expression;
		this.type = type;
	}

	public Expression getExpression() {
		return expression;
	}

	public TypeExpr getType() {
		return type;
	}

	public ExprTypeAssertion copy(Expression expression, TypeExpr type) {
		if (Objects.equals(getExpression(), expression) && Objects.equals(getType(), type)) {
			return this;
		} else {
			return new ExprTypeAssertion(this, expression, type);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getExpression());
		action.accept(getType());
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy((Expression) transformation.apply(getExpression()), (TypeExpr) transformation.apply(getType()));
	}
}
