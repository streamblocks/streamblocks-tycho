package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;

public class BasicInterpreter implements Interpreter {

	private final ExpressionEvaluator evaluator;
	private final StatementExecutor executor;
	private final Stack stack;

	public BasicInterpreter(int stackSize) {
		this.stack = new BasicStack(stackSize);
		this.evaluator = new ExpressionEvaluator(this);
		this.executor = new StatementExecutor(this);
	}

	@Override
	public void execute(Statement stmt, Environment env) {
		stmt.accept(executor, env);
	}

	@Override
	public RefView evaluate(Expression expr, Environment env) {
		return expr.accept(evaluator, env);
	}

	@Override
	public Stack getStack() {
		return stack;
	}

}
