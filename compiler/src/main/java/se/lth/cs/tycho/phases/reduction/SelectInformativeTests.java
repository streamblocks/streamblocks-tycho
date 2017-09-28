package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.util.TychoCollectors;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SelectInformativeTests implements Function<State, State> {
	private final boolean trueBranch;
	private final boolean falseBranch;

	private SelectInformativeTests(boolean trueBranch, boolean falseBranch) {
		this.trueBranch = trueBranch;
		this.falseBranch = falseBranch;
	}

	public static SelectInformativeTests trueInformative() {
		return new SelectInformativeTests(true, false);
	}

	public static SelectInformativeTests falseInformative() {
		return new SelectInformativeTests(false, true);
	}

	public static SelectInformativeTests informative() {
		return new SelectInformativeTests(true, true);
	}

	@Override
	public State apply(State state) {
		return new MultiInstructionState(
				state.getInstructions().stream().collect(
						TychoCollectors.minimaBy(
								Comparator.comparingInt(this::nbrOfFollowingInstrs),
								Collectors.toList()
						)
				)
		);
	}

	private int nbrOfFollowingInstrs(Instruction i) {
		return i.accept(
				exec -> 0,
				test ->
						(trueBranch ? count(test.targetTrue()) : 0) +
						(falseBranch ? count(test.targetFalse()) : 0),
				wait -> 0
		);
	}

	private static int count(State s) {
		return s.getInstructions().size();
	}
}
