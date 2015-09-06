package se.lth.cs.tycho.reporting;

import java.util.EnumMap;
import java.util.Set;

public class ConsoleReporter implements Reporter {
	private final Set<Diagnostic.Kind> kinds;
	private EnumMap<Diagnostic.Kind, Integer> counts;

	public ConsoleReporter(Set<Diagnostic.Kind> kinds) {
		this.kinds = kinds;
		counts = new EnumMap<Diagnostic.Kind, Integer>(Diagnostic.Kind.class);
		for (Diagnostic.Kind kind : Diagnostic.Kind.values()) {
			counts.put(kind, 0);
		}
	}

	@Override
	public void report(Diagnostic diagnostic) {
		counts.put(diagnostic.getKind(), counts.get(diagnostic.getKind()) + 1);
		if (kinds.contains(diagnostic.getKind())) {
			System.out.println(diagnostic.generateMessage());
		}
	}

	@Override
	public int getMessageCount(Diagnostic.Kind kind) {
		return counts.get(kind);
	}
}
