package se.lth.cs.tycho.transform.reduction;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.transform.reduction.util.ControllerWrapper;
import se.lth.cs.tycho.transform.reduction.util.ShortestPathOfOnlyOutputTestsToExec;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;
import se.lth.cs.tycho.transform.util.GenInstruction;

public class LateOutputConditions<S> extends FilteredController<S> {
	private final ShortestPathOfOnlyOutputTestsToExec<S> shortestPath;

	public LateOutputConditions(Controller<S> original) {
		super(original);
		shortestPath = new ShortestPathOfOnlyOutputTestsToExec<>(original);
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		List<GenInstruction<S>> instructions = original.instructions(state);
		Optional<Integer> min = instructions.stream()
				.map(shortestPath::distanceFromInstruction)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.min(Comparator.naturalOrder());
		if (min.isPresent()) {
			return instructions.stream()
					.filter(i -> shortestPath.distanceFromInstruction(i).equals(min))
					.collect(Collectors.toList());
		} else {
			return instructions.stream().filter(i -> !isOutputTest(i)).collect(Collectors.toList());
		}
	}

	private boolean isOutputTest(GenInstruction<S> i) {
		if (i.isTest()) {
			Condition cond = getCondition(i.asTest().C());
			if (cond instanceof PortCondition) {
				PortCondition portCond = (PortCondition) cond;
				return !portCond.isInputCondition();
			}
		}
		return false;
	}

	public static <S> ControllerWrapper<S, S> wrapper() {
		return LateOutputConditions<S>::new;
	}

}
