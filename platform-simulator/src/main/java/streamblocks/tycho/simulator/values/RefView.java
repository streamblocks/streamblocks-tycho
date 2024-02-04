package streamblocks.tycho.simulator.values;

import se.lth.cs.tycho.reporting.CompilationException;

public interface RefView {

    public Value getValue() throws CompilationException;

    public long getLong() throws CompilationException;

    public double getDouble() throws CompilationException;

    public String getString() throws CompilationException;

    public void assignTo(Ref r);
}
