package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.values.Collection;
import se.lth.cs.tycho.interp.values.Iterator;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

public class ComprehensionHelper {

    private final Interpreter interpreter;
    private final Stack stack;
    private final TypeConverter converter;

    public ComprehensionHelper(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.stack = interpreter.getStack();
        this.converter = TypeConverter.getInstance();
    }

    public void interpret(Generator g, ImmutableList<Expression> filters, Runnable action, Environment env) throws CompilationException {
        interpret(g, filters, action, env, 0, 0, null);
    }


    private void interpret(Generator g, ImmutableList<Expression> filters, Runnable action, Environment env, int gen, int var, Collection coll) throws CompilationException {
        if (gen == 1) {
            // -- all generators have given their variables values, run the body
            action.run();
        } else if (var == g.getVarDecls().size()) {
            boolean included = true;
            for (Expression filter : filters) {
                if (!converter.getBoolean(interpreter.evaluate(filter, env))) {
                    included = false;
                    break;
                }
            }
            if (included) {
                interpret(g, filters, action, env, gen + 1, 0, null);
            }
        } else {
            if (coll == null) {
                RefView c = interpreter.evaluate(g.getCollection(), env);
                coll = converter.getCollection(c);
            }
            Iterator iter = coll.iterator();
            while (!iter.finished()) {
                stack.push(iter);
                interpret(g, filters, action, env, gen, var + 1, coll);
                stack.pop();
                iter.advance();
            }
        }
    }


}
