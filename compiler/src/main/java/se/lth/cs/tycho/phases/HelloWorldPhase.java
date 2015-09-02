package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationUnit;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.settings.Setting;
import se.lth.cs.tycho.settings.StringSetting;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HelloWorldPhase implements Phase {

	private static final StringSetting SETTING = new StringSetting() {
		@Override
		public String getKey() {
			return "hello-world-target";
		}

		@Override
		public String getDescription() {
			return "The object to greet with the HelloWorld compilation phase.";
		}

		@Override
		public String defaultValue() {
			return "World";
		}
	};

	@Override
	public String getDescription() {
		return "Reports \"Hello, World!\" as information.";
	}

	@Override
	public Optional<CompilationUnit> execute(CompilationUnit unit, Context context) {
		context.getReporter().report(new Diagnostic(Diagnostic.Kind.INFO,
				"Hello, " + context.getConfiguration().get(SETTING) + "!"));
		return Optional.of(unit);
	}

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Collections.singletonList(SETTING);
	}
}
