package se.lth.cs.tycho.transform.reduction;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public abstract class ShortestDistance<S, D extends Comparable<? super D>> extends FilteredController<S> {
	
	private final Map<S, List<GenInstruction<S>>> cache;

	public ShortestDistance(Controller<S> original) {
		super(original);
		this.cache = new HashMap<>();
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		if (cache.containsKey(state)) {
			return cache.get(state);
		} else {
			List<GenInstruction<S>> result;
			Optional<D> min = distanceFromState(state);
			if (min.isPresent()) {
				D m = min.get();
				result = original.instructions(state)
						.stream()
						.filter(i -> distanceFromInstruction(i).equals(m))
						.collect(Collectors.toList());
			} else {
				result = Collections.emptyList();
			}
			cache.put(state, result);
			return result;
		}
	}

	protected Optional<D> distanceFromState(S state) {
		return original.instructions(state)
				.stream()
				.map(i -> distanceFromInstruction(i))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.min(Comparator.naturalOrder());
	}

	protected Optional<D> distanceFromInstruction(GenInstruction<S> i) {
		if (i.isTest()) {
			return distanceFromTest(i.asTest());
		} else if (i.isCall()) {
			return distanceFromCall(i.asCall());
		} else {
			return distanceFromWait(i.asWait());
		}
	}

	protected abstract Optional<D> distanceFromWait(Wait<S> i);

	protected abstract Optional<D> distanceFromCall(Call<S> i);

	protected abstract Optional<D> distanceFromTest(Test<S> i);
}
