package se.lth.cs.tycho.transform.reduction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction;

public class NearestExecReducer<S> implements Controller<S> {

	private final Controller<S> controller;
	private final Map<S, Integer> cache = new HashMap<>();

	public NearestExecReducer(Controller<S> controller) {
		this.controller = controller;
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		int dist = distance(state);
		return controller.instructions(state).stream().filter(i -> distance(i) <= dist).collect(Collectors.toList());
	}

	private int distance(S s) {
		if (cache.containsKey(s)) {
			return cache.get(s);
		} else {
			int dist = controller.instructions(s).stream().mapToInt(this::distance).min().orElse(Integer.MAX_VALUE);
			cache.put(s, dist);
			return dist;
		}
	}

	private int distance(GenInstruction<S> i) {
		if (i.isCall()) {
			return 0;
		} else if (i.isWait()) {
			return Integer.MAX_VALUE;
		} else {
			return Arrays.stream(i.destinations()).mapToInt(this::distance).min().getAsInt();
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

	public static <S> Transformation<Controller<S>> transformation() {
		return NearestExecReducer<S>::new;
	}

}
