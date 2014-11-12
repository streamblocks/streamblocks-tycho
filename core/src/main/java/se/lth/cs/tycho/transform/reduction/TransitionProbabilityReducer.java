package se.lth.cs.tycho.transform.reduction;

import java.nio.file.Path;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.messages.util.Result;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class TransitionProbabilityReducer<S> implements Controller<S> {
	private final Map<S, BitSet> reachable = new HashMap<>();
	private final Controller<S> controller;
	private final ProbabilityTable table;
	private final Reachability reachability = new Reachability();

	public TransitionProbabilityReducer(Controller<S> controller, ProbabilityTable table) {
		this.controller = controller;
		this.table = table;
	}

	private BitSet reachable(S state) {
		if (reachable.containsKey(state)) {
			return reachable.get(state);
		} else {
			BitSet result = new BitSet();
			for (GenInstruction<S> instr : controller.instructions(state)) {
				instr.accept(reachability, result);
			}
			reachable.put(state, result);
			return result;
		}
	}

	private BitSet reachable(GenInstruction<S> instr) {
		return instr.accept(reachability, new BitSet());
	}

	private final class Reachability implements GenInstruction.Visitor<S, BitSet, BitSet> {

		@Override
		public BitSet visitCall(Call<S> call, BitSet builder) {
			builder.set(call.T());
			return builder;
		}

		@Override
		public BitSet visitTest(Test<S> test, BitSet builder) {
			builder.or(reachable(test.S0()));
			builder.or(reachable(test.S1()));
			return builder;
		}

		@Override
		public BitSet visitWait(Wait<S> wait, BitSet builder) {
			return builder;
		}

	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		BitSet reachable = reachable(state);
		Optional<Integer> transition = reachable.stream()
				.boxed()
				.sorted(Comparator.comparingDouble(table::probability).reversed())
				.findFirst();
		if (transition.isPresent()) {
			int t = transition.get();
			return controller.instructions(state)
					.stream()
					.filter(i -> reachable(i).get(t))
					.collect(Collectors.toList());
		} else {
			return controller.instructions(state);
		}
	}

	@Override
	public S initialState() {
		return controller.initialState();
	}

	@Override
	public QID instanceId() {
		return controller.instanceId();
	}

	public static <S> Transformation<Controller<S>> transformation(Path path, MessageReporter msg) {
		return controller -> {
			Path input = path.resolve(controller.instanceId().toPath()).resolve("transition-prob.txt");
			Result<ProbabilityTable> table = ProbabilityTable.fromFile(input, 0.0);
			if (table.isSuccess()) {
				return new TransitionProbabilityReducer<>(controller, table.get());
			} else {
				msg.report(table.getMessage());
				return controller;
			}
		};
	}

}
