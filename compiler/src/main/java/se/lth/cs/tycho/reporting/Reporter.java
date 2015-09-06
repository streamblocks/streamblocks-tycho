package se.lth.cs.tycho.reporting;

import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public interface Reporter {
	Setting<Set<Diagnostic.Kind>> reportingLevel = new Setting<Set<Diagnostic.Kind>>() {

		@Override
		public String getKey() {
			return "reporting-level";
		}

		@Override
		public String getType() {
			return "info | warning | error | quiet";
		}

		@Override
		public String getDescription() {
			return "Sets the level of reporting.";
		}

		@Override
		public Optional<Set<Diagnostic.Kind>> read(String string) {
			EnumSet<Diagnostic.Kind> set = EnumSet.noneOf(Diagnostic.Kind.class);
			switch (string) {
				case "info": set.add(Diagnostic.Kind.INFO);
				case "warning": set.add(Diagnostic.Kind.WARNING);
				case "error": set.add(Diagnostic.Kind.ERROR);
				case "quiet": break;
				default: return Optional.empty();
			}
			return Optional.of(Collections.unmodifiableSet(set));
		}

		@Override
		public Set<Diagnostic.Kind> defaultValue() {
			return Collections.unmodifiableSet(EnumSet.allOf(Diagnostic.Kind.class));
		}
	};
	void report(Diagnostic diagnostic);

	int getMessageCount(Diagnostic.Kind kind);

	static Reporter instance(Configuration configuration) {
		return new ConsoleReporter(configuration.get(reportingLevel));
	}
}
