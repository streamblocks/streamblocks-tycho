package se.lth.cs.tycho.interp.exception;


import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;

public class InterpIndexOutOfBoundsException extends CompilationException {
    public InterpIndexOutOfBoundsException(Diagnostic diagnostic) {
        super(diagnostic);
    }
}
