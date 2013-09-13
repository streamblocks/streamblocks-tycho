package net.opendf.interp;

import net.opendf.interp.values.RefView;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Statement;

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
