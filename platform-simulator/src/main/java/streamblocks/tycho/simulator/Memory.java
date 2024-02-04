package streamblocks.tycho.simulator;

import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import streamblocks.tycho.simulator.values.Ref;

public interface Memory {
    public Ref get(Variable var) throws CompilationException;

    public Ref declare(int scope, int offset);

    public Memory closure(ImmutableList<Variable> variables, Stack stack);
}
