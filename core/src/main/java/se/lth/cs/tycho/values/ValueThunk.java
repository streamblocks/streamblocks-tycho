package se.lth.cs.tycho.values;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.expr.Expression;

public class ValueThunk {

	private final Expression expr;
	private final Environment<ValueThunk> env;
	private final NamespaceDecl origin;

	public ValueThunk(Expression expr, Environment<ValueThunk> env, NamespaceDecl origin) {
		this.expr = expr;
		this.env = env;
		this.origin = origin;
	}

	public Expression getExpr() {
		return expr;
	}

	public Environment<ValueThunk> getEnv() {
		return env;
	}

	public NamespaceDecl getOrigin() {
		return origin;
	}

}
