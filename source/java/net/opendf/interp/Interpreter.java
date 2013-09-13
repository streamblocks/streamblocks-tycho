package net.opendf.interp;

import net.opendf.interp.values.RefView;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Statement;

public interface Interpreter {
	public void execute(Statement stmt, Environment env);
	public RefView evaluate(Expression expr, Environment env);
	public Stack getStack();
}
