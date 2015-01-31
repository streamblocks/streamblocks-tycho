package se.lth.cs.tycho.transform.reduction;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.messages.util.Result;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;
import se.lth.cs.tycho.transform.util.GenInstruction;

public class NearestExecReducer<S> extends FilteredController<S> {

	private final Map<S, Length> distance = new HashMap<>();
	private final ProbabilityTable table;

	public NearestExecReducer(Controller<S> controller, ProbabilityTable table) {
		super(controller);
		this.table = table;
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		Length dist = distance(state);
		return original.instructions(state).stream()
				.filter(i -> Length.comparator().compare(distance(i), dist) <= 0)
				.collect(Collectors.toList());
	}

	private Length distance(S s) {
		if (distance.containsKey(s)) {
			return distance.get(s);
		} else {
			Length dist = original.instructions(s).stream()
					.map(this::distance)
					.min(Length.comparator())
					.orElse(Length.waitLength());
			distance.put(s, dist);
			return dist;
		}
	}

	private Length distance(GenInstruction<S> i) {
		if (i.isCall()) {
			return Length.execLength(table.probability(((GenInstruction.Call<S>) i).T()));
		} else if (i.isWait()) {
			return Length.waitLength();
		} else {
			return Length.testLength(Arrays.stream(i.destinations()).map(this::distance).min(Length.comparator()).get());
		}
	}

	public static <S> Transformation<Controller<S>> transformation(Path path, MessageReporter msg) {
		return controller -> {
			Path input = path.resolve(controller.instanceId().toPath()).resolve("transition-prob.txt");
			Result<ProbabilityTable> table = ProbabilityTable.fromFile(input, 0.0);
			if (table.isSuccess()) {
				return new NearestExecReducer<>(controller, table.get());
			} else {
				msg.report(table.getMessage());
				return controller;
			}
		};
	}

}
