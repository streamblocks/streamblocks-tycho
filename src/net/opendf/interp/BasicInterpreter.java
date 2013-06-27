package net.opendf.interp;

import net.opendf.interp.values.RefView;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Statement;

public class BasicInterpreter implements Interpreter {

	private final ExpressionEvaluator evaluator;
	private final StatementExecutor executor;
	private final VarDeclarator declarator;
	private final Stack stack;

	public BasicInterpreter(Stack stack) {
		this.stack = stack;
		this.evaluator = new ExpressionEvaluator(this);
		this.executor = new StatementExecutor(this);
		this.declarator = new VarDeclarator(this);
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
	public int declare(Decl decl, Environment env) {
		return decl.accept(declarator, env);
	}

	@Override
	public Stack getStack() {
		return stack;
	}

}
