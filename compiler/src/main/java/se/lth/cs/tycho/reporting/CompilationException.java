package se.lth.cs.tycho.reporting;

import java.io.IOException;

public class CompilationException extends RuntimeException {
	private final Diagnostic diagnostic;

	public CompilationException(Diagnostic diagnostic) {
		super(diagnostic.generateMessage());
		this.diagnostic = diagnostic;
	}

	public static CompilationException from(IOException e) {
		return new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "An error has occured: " + e.getMessage()));
	}

	public Diagnostic getDiagnostic() {
		return diagnostic;
	}
}
