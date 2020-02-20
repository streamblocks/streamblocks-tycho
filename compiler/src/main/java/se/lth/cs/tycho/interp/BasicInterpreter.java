package se.lth.cs.tycho.interp;

import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.FreeVariables;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.interp.attribute.ExpressionEvaluator;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;

public class BasicInterpreter implements Interpreter {

    private final ExpressionEvaluator evaluator;
    private final Stack stack;

    public BasicInterpreter(CompilationTask task, int stackSize) {
        this.stack = new BasicStack(stackSize);
        this.evaluator = MultiJ.from(ExpressionEvaluator.class)
                .bind("interpreter").to(this)
                .bind("stack").to(stack)
                .bind("converter").to(TypeConverter.instance)
                .bind("types").to(task.getModule(Types.key))
                .bind("freeVariables").to(task.getModule(FreeVariables.key))
                .bind("declarations").to(task.getModule(VariableDeclarations.key))
                .instance();
    }

    @Override
    public void execute(Statement stmt, Environment env) {
        return;
    }

    @Override
    public RefView evaluate(Expression expr, Environment env) {
        return evaluator.evaluate(expr, env);
    }

    @Override
    public Stack getStack() {
        return stack;
    }
}
