package se.lth.cs.tycho.transform.reduction;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class ConditionProbabilityController<S> extends ProbabilityBasedReducer<S> {
	private final Score score = new Score();

	public ConditionProbabilityController(Controller<S> controller, Path dataPath, MessageReporter msg) {
		super(controller, dataPath, msg);
	}

	@Override
	protected List<GenInstruction<S>> select(List<GenInstruction<S>> instructions) {
		return toList(instructions.stream().max(Comparator.comparing(this::score)));
	}

	private <T> List<T> toList(Optional<T> opt) {
		return opt.map(Collections::singletonList).orElse(Collections.emptyList());
	}

	private double score(GenInstruction<S> instr) {
		return instr.accept(score);
	}
	
	protected double defaultValue() {
		return 0.5;
	}

	private class Score implements GenInstruction.Visitor<S, Double, Void> {

		@Override
		public Double visitCall(Call<S> call, Void parameter) {
			return 2.0;
		}

		@Override
		public Double visitTest(Test<S> test, Void parameter) {
			return probability(test.C());
		}

		@Override
		public Double visitWait(Wait<S> wait, Void parameter) {
			return -1.0;
		}

	}
	
	public static <S> Transformation<Controller<S>> transformation(Path path, MessageReporter msg) {
		return controller -> new ConditionProbabilityController<>(controller, path, msg);
	}

}
