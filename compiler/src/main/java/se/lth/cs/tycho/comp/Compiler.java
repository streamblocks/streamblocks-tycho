package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.phases.DeclarationAnalysisPhase;
import se.lth.cs.tycho.phases.HelloWorldPhase;
import se.lth.cs.tycho.phases.LoadEntityPhase;
import se.lth.cs.tycho.phases.LoadImportsPhase;
import se.lth.cs.tycho.phases.Phase;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.PathListSetting;
import se.lth.cs.tycho.settings.PathSetting;
import se.lth.cs.tycho.settings.Setting;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Compiler {
	private final Context compilationContext;
	public static final List<Phase> phases = Arrays.asList(
			new HelloWorldPhase(),
			new LoadEntityPhase(),
			new LoadImportsPhase(),
			//new PrintTreesPhase(),
			new DeclarationAnalysisPhase()
	);

	public static final Setting<List<Path>> sourcePaths = new PathListSetting() {
		@Override public String getKey() { return "source-paths"; }
		@Override public String getDescription() { return "Colon separated (:) list of search paths for source files."; }
		@Override public List<Path> defaultValue() { return Collections.singletonList(Paths.get("")); }
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
		CompilationUnit compilationUnit = new CompilationUnit(Collections.emptyList(), entity);
		long[] phaseExecutionTime = new long[phases.size()];
		int i = 0;
		for (Phase phase : phases) {
			long startTime = System.nanoTime();
			Optional<CompilationUnit> result = phase.execute(compilationUnit, compilationContext);
			phaseExecutionTime[i++] = System.nanoTime() - startTime;
			if (result.isPresent()) {
				compilationUnit = result.get();
			} else {
				return false;
			}
		}
		if (compilationContext.getConfiguration().get(phaseTimer)) {
			int j = 0;
			System.out.println("Execution time report:");
			for (Phase phase : phases) {
				System.out.println(phase.getName()+" ("+phaseExecutionTime[j++]/1_000_000+" ms)");
			}
		}
		return true;
	}
}
