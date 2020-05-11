package se.lth.cs.tycho.interp.values;

import se.lth.cs.tycho.reporting.CompilationException;

public interface RefView {
    Value getValue() throws CompilationException;

    long getLong() throws CompilationException;

    double getDouble() throws CompilationException;

    boolean getBoolean() throws CompilationException;

    String getString() throws CompilationException;

    void assignTo(Ref r);
}
