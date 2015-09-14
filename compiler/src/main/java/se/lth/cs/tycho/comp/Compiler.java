package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.phases.DeclarationAnalysisPhase;
import se.lth.cs.tycho.phases.LoadEntityPhase;
import se.lth.cs.tycho.phases.LoadImportsPhase;
import se.lth.cs.tycho.phases.NameAnalysisPhase;
import se.lth.cs.tycho.phases.OperatorParsingPhase;
import se.lth.cs.tycho.phases.Phase;
import se.lth.cs.tycho.phases.PrintLoadedSourceUnits;
import se.lth.cs.tycho.phases.WaitForInputPhase;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.PathListSetting;
import se.lth.cs.tycho.settings.PathSetting;
import se.lth.cs.tycho.settings.Setting;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Compiler {
	private final Context compilationContext;
	public static final List<Phase> phases = Arrays.asList(
			new WaitForInputPhase(),
			new LoadEntityPhase(),
			new LoadImportsPhase(),
			new PrintLoadedSourceUnits(),
			new OperatorParsingPhase(),
			new DeclarationAnalysisPhase(),
			new NameAnalysisPhase()
	);

	public static final Setting<List<Path>> sourcePaths = new PathListSetting() {
		@Override public String getKey() { return "source-paths"; }
		@Override public String getDescription() { return "A " + File.pathSeparator + "-separated list of search paths for source files."; }
		@Override public List<Path> defaultValue() { return Collections.emptyList(); }
	};

	public static final Setting<List<Path>> orccSourcePaths = new PathListSetting() {
		@Override public String getKey() { return "orcc-source-paths"; }
		@Override public String getDescription() { return "A " + File.pathSeparator + "-separated list of search paths for Orcc-compatible source files."; }
		@Override public List<Path> defaultValue() { return Collections.emptyList(); }
	};

	public static final Setting<List<Path>> xdfSourcePaths = new PathListSetting() {
		@Override public String getKey() { return "xdf-source-paths"; }
		@Override public String getDescription() { return "A " + File.pathSeparator + "-separated list of search paths for XDF networks."; }
		@Override public List<Path> defaultValue() { return Collections.emptyList(); }
	};

	public static final Setting<Path> targetPath = new PathSetting() {
		@Override public String getKey() { return "target-path"; }
		@Override public String getDescription() { return "Output directory for the compiled files."; }
		@Override public Path defaultValue() { return Paths.get(""); }
	};

	public static final Setting<Boolean> phaseTimer = new OnOffSetting() {
		@Override public String getKey() { return "phase-timer"; }
		@Override public String getDescription() { return "Measures the execution time of the compilation phases."; }
		@Override public Boolean defaultValue() { return false; }
	};

	public Compiler(Configuration configuration) {
		Reporter reporter = Reporter.instance(configuration);
		Loader loader = Loader.instance(configuration, reporter);
		this.compilationContext = new Context(configuration, loader, reporter);
	}

	public boolean compile(QID entity) {
		CompilationTask compilationTask = new CompilationTask(Collections.emptyList(), entity);
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
				System.out.println(phases.get(j).getName()+" ("+phaseExecutionTime[j]/1_000_000+" ms)");
			}
		}
		return success;
	}
}
