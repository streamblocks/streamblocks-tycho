package se.lth.cs.tycho.transform.reduction;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.messages.util.Result;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class ConditionProbabilityReducer<S> extends FilteredController<S> {
	private final Score score = new Score();
	
	private static final double CALL_SCORE = 2.0;
	private static final double DEFAULT_TEST_SCORE = 0.5;
	private static final double WAIT_SCORE = -1.0;
	
	private final double margin;

	private final ProbabilityTable table;

	public ConditionProbabilityReducer(Controller<S> controller, ProbabilityTable table, double margin) {
		super(controller);
		this.table = table;
		this.margin = margin;

	}

	private double score(GenInstruction<S> instr) {
		return instr.accept(score);
	}
	
	protected double defaultValue() {
		return DEFAULT_TEST_SCORE;
	}

	private class Score implements GenInstruction.Visitor<S, Double, Void> {

		@Override
		public Double visitCall(Call<S> call, Void parameter) {
			return CALL_SCORE;
		}

		@Override
		public Double visitTest(Test<S> test, Void parameter) {
			return table.probability(test.C());
		}

		@Override
		public Double visitWait(Wait<S> wait, Void parameter) {
			return WAIT_SCORE;
		}

	}
	
	public static <S> ControllerWrapper<S, S> wrapper(Path path, double margin, MessageReporter msg) {
		return controller -> {
			Path input = path.resolve(controller.instanceId().toPath()).resolve("condition-prob.txt");
			Result<ProbabilityTable> table = ProbabilityTable.fromFile(input, 0.0);
			if (table.isSuccess()) {
				return new ConditionProbabilityReducer<>(controller, table.get(), margin);
			} else {
				msg.report(table.getMessage());
				return controller;
			}
		};
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		List<GenInstruction<S>> instructions = original.instructions(state);
		double max = instructions.stream().mapToDouble(this::score).max().orElse(WAIT_SCORE);
		return instructions.stream().filter(i -> score(i) + margin >= max).collect(Collectors.toList());
	}

}
