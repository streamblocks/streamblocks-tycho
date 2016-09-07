package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.phases.*;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.PathListSetting;
import se.lth.cs.tycho.settings.PathSetting;
import se.lth.cs.tycho.settings.Setting;
import se.lth.cs.tycho.settings.SettingsManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Compiler {
	private final Context compilationContext;
	public static final List<Phase> phases = Arrays.asList(
			// Hack: pause to hook up profiler.
//			new WaitForInputPhase(),

			// Parse
			new LoadEntityPhase(),
			new LoadPreludePhase(),
			new LoadImportsPhase(),

			// For debugging
			new PrintLoadedSourceUnits(),
			new PrintTreesPhase(),

			// Post parse
			new OperatorParsingPhase(),

			// Name and type analyses and transformations
			new DeclarationAnalysisPhase(),
			new NameAnalysisPhase(),
			new TypeAnalysisPhase(),

			new CreateNetworkPhase(),
			new ResolveGlobalEntityNamesPhase(),
			new ResolveGlobalVariableNamesPhase(),
			new ElaborateNetworkPhase(),

			// Actor transformations
			new RenameActorVariablesPhase(),
			new LiftProcessVarDeclsPhase(),
			new ProcessToCalPhase(),
			new PrettyPrintPhase(),
			new AddSchedulePhase(),
			new ScheduleUntaggedPhase(),
			new ScheduleInitializersPhase(),
			new CloneTreePhase(),
			new CalToAmPhase(),
			new RemoveEmptyTransitionsPhase(),
			new ReduceActorMachinePhase(),
			new CompositionEntitiesUniquePhase(),
			new CompositionPhase(),
			new InternalizeBuffersPhase(),
			new RemoveUnusedConditionsPhase(),

			// Code generations
			new RemoveUnusedEntityDeclsPhase(),
			new PrintNetworkPhase(),
			new CBackendPhase()
	);

	public static final Setting<List<Path>> sourcePaths = new PathListSetting() {
		@Override public String getKey() { return "source-paths"; }
		@Override public String getDescription() { return "A " + File.pathSeparator + "-separated list of search paths for source files."; }
		@Override public List<Path> defaultValue(Configuration configuration) { return configuration.isDefined(orccSourcePaths) ? Collections.emptyList() : Collections.singletonList(Paths.get("")); }
	};

	public static final Setting<List<Path>> orccSourcePaths = new PathListSetting() {
		@Override public String getKey() { return "orcc-source-paths"; }
		@Override public String getDescription() { return "A " + File.pathSeparator + "-separated list of search paths for Orcc-compatible source files."; }
		@Override public List<Path> defaultValue(Configuration configuration) { return Collections.emptyList(); }
	};

	public static final Setting<List<Path>> xdfSourcePaths = new PathListSetting() {
		@Override public String getKey() { return "xdf-source-paths"; }
		@Override public String getDescription() { return "A " + File.pathSeparator + "-separated list of search paths for XDF networks."; }
		@Override public List<Path> defaultValue(Configuration configuration) { return configuration.get(orccSourcePaths); }
	};

	public static final Setting<Path> targetPath = new PathSetting() {
		@Override public String getKey() { return "target-path"; }
		@Override public String getDescription() { return "Output directory for the compiled files."; }
		@Override public Path defaultValue(Configuration configuration) { return Paths.get(""); }
	};

	public static final Setting<Boolean> phaseTimer = new OnOffSetting() {
		@Override public String getKey() { return "phase-timer"; }
		@Override public String getDescription() { return "Measures the execution time of the compilation phases."; }
		@Override public Boolean defaultValue(Configuration configuration) { return false; }
	};

	public Compiler(Configuration configuration) {
		Reporter reporter = Reporter.instance(configuration);
		Loader loader = Loader.instance(configuration, reporter);
		this.compilationContext = new Context(configuration, loader, reporter);
//		assert dependenciesSatisfied() : "Unsatisfied phase dependencies.";
	}

	private static boolean dependenciesSatisfied() {
		Set<Class<? extends Phase>> executed = new HashSet<>();
		for (Phase phase : phases) {
			if (!executed.containsAll(phase.dependencies())) {
				return false;
			}
			executed.add(phase.getClass());
		}
		return true;
	}

	public static SettingsManager defaultSettingsManager() {
		return SettingsManager.builder()
				.add(sourcePaths)
				.add(orccSourcePaths)
				.add(xdfSourcePaths)
				.add(targetPath)
				.add(Reporter.reportingLevel)
				.add(phaseTimer)
				.add(Loader.followLinks)
				.addAll(phases.stream()
						.flatMap(phase -> phase.getPhaseSettings().stream())
						.collect(Collectors.toList()))
				.build();
	}

	public boolean compile(QID entity) {
		CompilationTask compilationTask = new CompilationTask(Collections.emptyList(), entity, null);
		long[] phaseExecutionTime = new long[phases.size()];
		int currentPhaseNumber = 0;
		boolean success = true;
		for (Phase phase : phases) {
			long startTime = System.nanoTime();
			compilationTask = phase.execute(compilationTask, compilationContext);
			phaseExecutionTime[currentPhaseNumber] = System.nanoTime() - startTime;
			currentPhaseNumber += 1;
			if (compilationContext.getReporter().getMessageCount(Diagnostic.Kind.ERROR) > 0) {
				success = false;
				break;
			}
		}
		if (compilationContext.getConfiguration().get(phaseTimer)) {
			System.out.println("Execution time report:");
			for (int j = 0; j < currentPhaseNumber; j++) {
				System.out.println(phases.get(j).getName() + " (" + phaseExecutionTime[j] / 1_000_000 + " ms)");
			}
		}
		return success;
	}

}
