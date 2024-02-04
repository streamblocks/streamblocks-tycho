package streamblocks.tycho.simulator.values;

import se.lth.cs.tycho.meta.interp.Interpreter;
import se.lth.cs.tycho.reporting.CompilationException;

public interface Function extends Value {
    /**
     * Evaluate the body of the function and removes the argument from the stack.
     * @param interpreter is used to evaluating the body.
     * @return
     * @throws CompilationException
     */
    public RefView apply(Interpreter interpreter) throws CompilationException;

    /**
     * @return the number of arguments this function takes, i.e. the number of values poped from the stack
     */
    public int getNbrParameters();
}
