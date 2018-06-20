package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.transformation.reduction.MergeStates;
import se.lth.cs.tycho.transformation.reduction.SelectFirstInstruction;
import se.lth.cs.tycho.transformation.reduction.SelectInformativeTests;
import se.lth.cs.tycho.transformation.reduction.SelectRandom;
import se.lth.cs.tycho.transformation.reduction.ShortestPath;
import se.lth.cs.tycho.transformation.reduction.TransformedController;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.EnumSetting;
import se.lth.cs.tycho.settings.IntegerSetting;
import se.lth.cs.tycho.settings.ListSetting;
import se.lth.cs.tycho.settings.OptionalSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class ReduceActorMachinePhase implements Phase {
	private static final Setting<Integer> amStateMergeIterations = new IntegerSetting() {
		@Override
		public String getKey() {
			return "am-state-merge-iterations";
		}

		@Override
		public String getDescription() {
			return "Number of iterations to merge identical states in the actor machines.";
		}

		@Override
		public Integer defaultValue(Configuration configuration) {
			return 10; // As some point RVC_MPEG4_SP_Decoder needed 13 iterations to be fully reduced.
		}
	};

	public enum ReductionAlgorithm {
		FIRST, RANDOM, SHORTEST_PATH_TO_EXEC, INFORMATIVE_TESTS, INFORMATIVE_TESTS_IF_TRUE, INFORMATIVE_TESTS_IF_FALSE
	}

	private static final Setting<List<ReductionAlgorithm>> reductionAlgorithm = new ListSetting<ReductionAlgorithm>(
			new EnumSetting<ReductionAlgorithm>(ReductionAlgorithm.class) {
				@Override
				public String getKey() {
					return "reduction-algorithm";
				}

				@Override
				public String getDescription() {
					return "Actor machine reduction algorithm.";
				}

				@Override
				public ReductionAlgorithm defaultValue(Configuration configuration) {
					throw new AssertionError();
				}
			}, ",") {
		@Override
		public List<ReductionAlgorithm> defaultValue(Configuration configuration) {
			return Collections.singletonList(ReductionAlgorithm.INFORMATIVE_TESTS);
		}
	};

	private static final Setting<Optional<Integer>> randomSeed = new OptionalSetting<>(new IntegerSetting() {
		@Override
		public String getKey() {
			return "random-reduction-seed";
		}

		@Override
		public String getDescription() {
			return "Seed for the random number generator in the random reducer.";
		}

		@Override
		public Integer defaultValue(Configuration configuration) {
			throw new UnsupportedOperationException();
		}
	}, "time");

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Arrays.asList(
				amStateMergeIterations,
				reductionAlgorithm,
				randomSeed);
	}

	@Override
	public String getDescription() {
		return "Reduces the actor machines to deterministic actor machines.";
	}

	private static List<Function<State, State>> reductionAlgorithms(Configuration configuration) {
		List<Function<State, State>> result = new ArrayList<>();
		for (ReductionAlgorithm algorithm : configuration.get(reductionAlgorithm)) {
			switch (algorithm) {
				case FIRST:
					result.add(new SelectFirstInstruction());
					break;
				case RANDOM:
					result.add(new SelectRandom(configuration.get(randomSeed).map(Integer::longValue).orElse(System.currentTimeMillis())));
					break;
				case SHORTEST_PATH_TO_EXEC:
					result.add(new ShortestPath());
					break;
				case INFORMATIVE_TESTS:
					result.add(SelectInformativeTests.informative());
					break;
				case INFORMATIVE_TESTS_IF_TRUE:
					result.add(SelectInformativeTests.falseInformative());
					break;
				case INFORMATIVE_TESTS_IF_FALSE:
					result.add(SelectInformativeTests.trueInformative());
					break;
				default:
					throw new AssertionError();
			}
		}
		return result;
	}

	private static List<Function<State, State>> reductionList(Configuration configuration) {
		int iterations = configuration.get(amStateMergeIterations);
		List<Function<State, State>> reducers = new ArrayList<>();
		reducers.addAll(reductionAlgorithms(configuration));
		reducers.add(new SelectFirstInstruction());
		Stream.generate(MergeStates::new).limit(iterations).forEach(reducers::add);
		return reducers;
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.transformChildren(MultiJ.from(ReduceActorMachine.class)
				.bind("config").to(context.getConfiguration())
				.instance());
	}

	@Module
	interface ReduceActorMachine extends IRNode.Transformation {
		@Binding(BindingKind.INJECTED)
		Configuration config();

		default List<Function<State, State>> transformations() {
			return reductionList(config());
		}

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(Decl decl) {
			return decl;
		}

		default IRNode apply(GlobalEntityDecl decl) {
			return decl.transformChildren(this);
		}

		default IRNode apply(Entity entity) {
			return entity;
		}

		default IRNode apply(ActorMachine actorMachine) {
			return actorMachine.withController(TransformedController.from(actorMachine.controller(),
					transformations()));
		}
	}

}
