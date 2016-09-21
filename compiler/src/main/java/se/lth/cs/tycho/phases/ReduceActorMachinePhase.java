package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.phases.reduction.MergeStates;
import se.lth.cs.tycho.phases.reduction.SelectRandom;
import se.lth.cs.tycho.phases.reduction.SingleInstructionState;
import se.lth.cs.tycho.phases.reduction.TransformedController;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.EnumSetting;
import se.lth.cs.tycho.settings.IntegerSetting;
import se.lth.cs.tycho.settings.OptionalSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReduceActorMachinePhase implements Phase {
	private static final Setting<Integer> amStateMergeIterations = new IntegerSetting() {
		@Override
		public String getKey() {
			return "am-state-merge-interations";
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
		SELECT_FIRST, SELECT_RANDOM;
	}

	private static final Setting<ReductionAlgorithm> reductionAlgorithm = new EnumSetting<ReductionAlgorithm>(ReductionAlgorithm.class) {
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
			return ReductionAlgorithm.SELECT_FIRST;
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

	private Function<State, State> reductionAlgorithm(Configuration configuration) {
		switch (configuration.get(reductionAlgorithm)) {
			case SELECT_FIRST: return selectFirst;
			case SELECT_RANDOM: return new SelectRandom(configuration.get(randomSeed).map(Integer::longValue).orElse(System.currentTimeMillis()));
			default: throw new AssertionError();
		}
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		int iterations = context.getConfiguration().get(amStateMergeIterations);
		List<Function<State, State>> transformations =
				Stream.concat(
						Stream.of(reductionAlgorithm(context.getConfiguration())),
						Stream.generate(MergeStates::new).limit(iterations))
				.collect(Collectors.toList());
		return task.transformChildren(MultiJ.from(ReduceActorMachine.class)
				.bind("transformations").to(new TransformationList(transformations)).instance());
	}

	public static class TransformationList {
		public final List<Function<State, State>> transformations;

		public TransformationList(List<Function<State, State>> transformations) {
			this.transformations = transformations;
		}
	}

	@Module
	interface ReduceActorMachine extends IRNode.Transformation {
		@Binding
		TransformationList transformations();

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
					transformations().transformations));
		}
	}

	private static final Function<State, State> selectFirst =
			state -> new SingleInstructionState(state.getInstructions().get(0));

}
