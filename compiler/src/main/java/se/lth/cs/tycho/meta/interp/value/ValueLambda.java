package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.type.TypeExpr;

import java.util.List;

public class ValueLambda extends Value {

	private List<ValueParameter> parameters;
	private Expression body;
	private TypeExpr type;

	public ValueLambda(List<ValueParameter> parameters, Expression body, TypeExpr type) {
		this.parameters = parameters;
		this.body = body;
		this.type = type;
	}

	public List<ValueParameter> parameters() {
		return parameters;
	}

	public Expression body() {
		return body;
	}

	public TypeExpr type() {
		return type;
	}

	@Override
	public String toString() {
		return "ValueLambda{}";
	}
}
