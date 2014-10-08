package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;

public interface Interpreter {
	public void execute(Statement stmt, Environment env);
	public RefView evaluate(Expression expr, Environment env);
	public Stack getStack();
}
