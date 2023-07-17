package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.expr.Expression;

public class ValueUndefined extends Value {

	private static final ValueUndefined undefined = new ValueUndefined();

	private final Expression expr;


	private ValueUndefined() {
		this(null);
	}

	public ValueUndefined(Expression expr) {
		this.expr = expr;
	}

	public Expression getUndefinedExpression(){
		return expr;
	}

	public static ValueUndefined undefined() {
		return undefined;
	}

	@Override
	public String toString() {
		return "<undefined>";
	}
}