package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public class ExprDeref extends Expression {
	private final Expression variable;

	public ExprDeref(Expression variable) {
		this(null, variable);
	}
	private ExprDeref(ExprDeref original, Expression variable) {
		super(original);
		this.variable = variable;
	}

	private ExprDeref copy(Expression variable) {
		if (this.variable == variable) {
			return this;
		} else {
			return new ExprDeref(this, variable);
		}
	}

	public Expression getReference() {
		return variable;
	}

	public ExprDeref withReference(Expression variable) {
		return copy(variable);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(variable);
	}

	@Override
	public ExprDeref transformChildren(Transformation transformation) {
		return copy(transformation.applyChecked(Expression.class, variable));
	}

}
