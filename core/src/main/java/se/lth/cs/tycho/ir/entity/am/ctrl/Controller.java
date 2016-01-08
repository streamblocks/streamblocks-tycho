package se.lth.cs.tycho.ir.entity.am.ctrl;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface Controller {
	State getInitialState();

	default List<? extends State> getStateList() {
		LinkedHashSet<State> result = new LinkedHashSet<>();
		Queue<State> queue = new ArrayDeque<>();
		queue.add(getInitialState());
		Consumer<Instruction> enqueueTargets = i -> i.forEachTarget(queue::add);
		while (!queue.isEmpty()) {
			State s = queue.remove();
			if (result.add(s)) {
				s.getInstructions().forEach(enqueueTargets);
			}
		}
		return result.stream().collect(Collectors.toList());
	}
}
