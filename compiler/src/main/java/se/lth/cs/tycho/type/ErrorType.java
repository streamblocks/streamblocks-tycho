package se.lth.cs.tycho.type;

import se.lth.cs.tycho.reporting.Diagnostic;

public class ErrorType implements Type {
    private final Diagnostic diagnostic;

    public ErrorType(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    @Override
    public String toString() {
        return "ErrorType(" + diagnostic.generateMessage() + ")";
    }
}
