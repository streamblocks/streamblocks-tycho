package streamblocks.tycho.simulator;


import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import streamblocks.tycho.simulator.values.RefView;

public interface Interpreter {
    public void execute(Statement stmt, Environment env);
    public RefView evaluate(Expression expr, Environment env);
    public Stack getStack();
}
