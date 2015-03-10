package se.lth.cs.tycho.transform.reduction.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public abstract class ShortestPath<D, S> {
	protected final Controller<S> controller;
	protected final Comparator<D> comparator;
	private final Map<S, Optional<D>> cache;

	public ShortestPath(Controller<S> controller, Comparator<D> comparator) {
		this.controller = controller;
		this.comparator = comparator;
		this.cache = new HashMap<>();
	}

	public Optional<D> distanceFromState(S state) {
		if (cache.containsKey(state)) {
			return cache.get(state);
		} else {
			Optional<D> result = controller.instructions(state)
					.stream()
					.map(this::distanceFromInstruction)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.min(comparator);
			cache.put(state, result);
			return result;
		}
	}

	public Optional<D> distanceFromInstruction(GenInstruction<S> instruction) {
		if (instruction.isTest()) {
			return distanceFromTest(instruction.asTest());
		} else if (instruction.isCall()) {
			return distanceFromCall(instruction.asCall());
		} else {
			return distanceFromWait(instruction.asWait());
		}
	}

	protected abstract Optional<D> distanceFromWait(Wait<S> wait);

	protected abstract Optional<D> distanceFromCall(Call<S> call);

	protected abstract Optional<D> distanceFromTest(Test<S> test);
	
}
