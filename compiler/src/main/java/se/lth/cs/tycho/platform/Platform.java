package se.lth.cs.tycho.platform;

import se.lth.cs.tycho.compiler.Loader;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.SettingsManager;
import se.lth.cs.tycho.compiler.Compiler;

import java.util.List;
import java.util.stream.Collectors;

public interface Platform {
	String name();
	String description();
	List<Phase> phases();
	default SettingsManager settingsManager() {
		return SettingsManager.builder()
				.add(Compiler.sourcePaths)
				.add(Compiler.orccSourcePaths)
				.add(Compiler.xdfSourcePaths)
				.add(Compiler.targetPath)
				.add(Reporter.reportingLevel)
				.add(Compiler.phaseTimer)
				.add(Loader.followLinks)
				.addAll(phases().stream()
						.flatMap(phase -> phase.getPhaseSettings().stream())
						.collect(Collectors.toList()))
				.build();
	}
}
