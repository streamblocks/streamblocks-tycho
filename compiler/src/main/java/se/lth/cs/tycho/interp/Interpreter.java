package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;

public interface Interpreter {
    void execute(Statement stmt, Environment env);

    RefView evaluate(Expression expr, Environment env);

    Stack getStack();
}
