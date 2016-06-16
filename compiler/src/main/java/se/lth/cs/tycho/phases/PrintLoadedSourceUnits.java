package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PrintLoadedSourceUnits implements Phase {
	public static Setting<Boolean> printLoadedSourceUnits = new OnOffSetting() {
		@Override public String getKey() { return "print-loaded-source-units"; }
		@Override public String getDescription() { return "Enables printing of loaded source units"; }
		@Override public Boolean defaultValue(Configuration configuration) { return false; }
	};

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Collections.singletonList(printLoadedSourceUnits);
	}

	@Override
	public String getDescription() {
		return "Prints the loaded source units.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		if (context.getConfiguration().get(printLoadedSourceUnits)) {
			String units = task.getSourceUnits().stream()
					.map(SourceUnit::getLocation)
					.collect(Collectors.joining("\n"));
			context.getReporter().report(new Diagnostic(Diagnostic.Kind.INFO, "Source units: \n" + units));
		}
		return task;
	}
}
