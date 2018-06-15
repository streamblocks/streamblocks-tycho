package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.platform.Platform;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.*;
import se.lth.cs.tycho.reporting.CompilationException;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;

public class Compiler {
	private final Context compilationContext;

	private final Platform platform;

	private static List<Platform> platforms = null;

	public static List<Platform> getPlatforms() {
		if (platforms == null) {
			ImmutableList.Builder<Platform> platformListBuilder = ImmutableList.builder();
			ServiceLoader<Platform> platformLoader = ServiceLoader.load(Platform.class);
			platformLoader.forEach(platformListBuilder::add);
			platforms = platformListBuilder.build();
		}
		return platforms;
	}

	public static Optional<Platform> selectPlatform(String name) {
		return platforms.stream().filter(p -> p.name().equals(name)).findFirst();
	}

	public static Platform defaultPlatform() {
		return getPlatforms().stream()
			.filter(platform -> platform.name().equals("sequential-c"))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Could not find the default platform."));
	}

	public static ImmutableList<Phase> frontendPhases() {
		return ImmutableList.of(
				// Parse
				new LoadEntityPhase(),
				new LoadPreludePhase(),
				new LoadImportsPhase(),

				// For debugging
				new PrintLoadedSourceUnits(),
				new PrintTreesPhase(),

				// Post parse
				new RemoveExternStubPhase(),
				new OperatorParsingPhase(),

				// Name and type analyses and transformations
				new DeclarationAnalysisPhase(),
				new ImportAnalysisPhase(),
				new NameAnalysisPhase(),
				new TypeAnnotationAnalysisPhase(),
				new TypeAnalysisPhase(),
				new AddTypeAnnotationsPhase(),

				// Orcc list parameters
				new OrccListParameters()
		);
	}

	public static List<Phase> networkElaborationPhases() {
		return ImmutableList.of(
				new CreateNetworkPhase(),
				new ResolveGlobalEntityNamesPhase(),
				new ResolveGlobalVariableNamesPhase(),
				new ElaborateNetworkPhase(),
				new RemoveUnusedGlobalDeclarations()
		);
	}

	public static List<Phase> actorMachinePhases() {
		return ImmutableList.of(
				new RenameActorVariablesPhase(),
				new LiftProcessVarDeclsPhase(),
				new ProcessToCalPhase(),
				new AddSchedulePhase(),
				new ScheduleUntaggedPhase(),
				new ScheduleInitializersPhase(),
				new MergeManyGuardsPhase(),
				new CalToAmPhase(),
				new RemoveEmptyTransitionsPhase(),
				new ReduceActorMachinePhase(),
				new CompositionEntitiesUniquePhase(),
				new CompositionPhase(),
				new InternalizeBuffersPhase(),
				new RemoveUnusedConditionsPhase(),
				new LiftScopesPhase()
		);
	}

	public static final Setting<List<Path>> sourcePaths = new PathListSetting() {
		@Override public String getKey() { return "source-path"; }
		@Override public String getDescription() { return "A " + File.pathSeparator + "-separated list of search paths for source files."; }
		@Override public List<Path> defaultValue(Configuration configuration) { return configuration.isDefined(orccSourcePaths) ? Collections.emptyList() : Collections.singletonList(Paths.get("")); }
	};

	public static final Setting<List<Path>> orccSourcePaths = new PathListSetting() {
		@Override public String getKey() { return "orcc-source-path"; }
		@Override public String getDescription() { return "A " + File.pathSeparator + "-separated list of search paths for Orcc-compatible source files."; }
		@Override public List<Path> defaultValue(Configuration configuration) { return Collections.emptyList(); }
	};

	public static final Setting<List<Path>> xdfSourcePaths = new PathListSetting() {
		@Override public String getKey() { return "xdf-source-path"; }
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

	public Compiler(Platform platform, Configuration configuration) {
		Reporter reporter = Reporter.instance(configuration);
		Loader loader = Loader.instance(configuration, reporter);
		this.compilationContext = new Context(configuration, loader, reporter);
		this.platform = platform;
		assert dependenciesSatisfied() : "Unsatisfied phase dependencies.";
	}

	private boolean dependenciesSatisfied() {
		Set<Class<? extends Phase>> executed = new HashSet<>();
		for (Phase phase : platform.phases()) {
			if (!executed.containsAll(phase.dependencies())) {
				return false;
			}
			executed.add(phase.getClass());
		}
		return true;
	}

	private boolean assertsEnabled() {
		boolean enabled = false;
		assert enabled = true; // intended side-effect
		return enabled;
	}

	public boolean compile(QID entity) {
		CompilationTask compilationTask = new CompilationTask(Collections.emptyList(), entity, null);
		long[] phaseExecutionTime = new long[platform.phases().size()];
		int currentPhaseNumber = 0;
		boolean checkTree = assertsEnabled();
		boolean success = true;
		try {
			for (Phase phase : platform.phases()) {
				long startTime = System.nanoTime();
				compilationTask = phase.execute(compilationTask, compilationContext);
				if (checkTree) checkTree(compilationTask);
				phaseExecutionTime[currentPhaseNumber] = System.nanoTime() - startTime;
				currentPhaseNumber += 1;
				if (compilationContext.getReporter().getMessageCount(Diagnostic.Kind.ERROR) > 0) {
					success = false;
					break;
				}
			}
		} catch (CompilationException e) {
			compilationContext.getReporter().report(e.getDiagnostic());
			success = false;
		}
		if (compilationContext.getConfiguration().get(phaseTimer)) {
			System.out.println("Execution time report:");
			for (int j = 0; j < currentPhaseNumber; j++) {
				System.out.println(platform.phases().get(j).getName() + " (" + phaseExecutionTime[j] / 1_000_000 + " ms)");
			}
		}
		return success;
	}

	private void checkTree(IRNode tree) {
		Map<IRNode, IRNode> parent = new IdentityHashMap<>();
		Queue<IRNode> queue = new ArrayDeque<>();
		queue.add(tree);
		while (!queue.isEmpty()) {
			IRNode node = queue.remove();
			node.forEachChild(child -> {
				if (parent.containsKey(child)) {
					throw new RuntimeException("Node " + child + " has multiple parents:\n" + node + "\n" + parent.get(child));
				} else {
					parent.put(child, node);
					queue.add(child);
				}
			});
		}
	}

}
