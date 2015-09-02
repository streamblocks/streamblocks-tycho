package se.lth.cs.tycho.reporting;

import java.util.Set;

public class ConsoleReporter implements Reporter {
	private final Set<Diagnostic.Kind> kinds;

	public ConsoleReporter(Set<Diagnostic.Kind> kinds) {
		this.kinds = kinds;
	}

	@Override
	public void report(Diagnostic diagnostic) {
		if (kinds.contains(diagnostic.getKind())) {
			System.out.println("["+diagnostic.getKind()+"] "+diagnostic.getMessage());
		}
	}
}
