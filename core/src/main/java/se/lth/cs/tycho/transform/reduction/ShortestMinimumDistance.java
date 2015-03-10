package se.lth.cs.tycho.transform.reduction;

import java.util.Optional;

import se.lth.cs.tycho.transform.reduction.util.ControllerWrapper;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class ShortestMinimumDistance<S> extends ShortestDistance<S, Integer> {

	public ShortestMinimumDistance(Controller<S> original) {
		super(original);
	}

	@Override
	protected Optional<Integer> distanceFromWait(Wait<S> i) {
		return Optional.empty();
	}

	@Override
	protected Optional<Integer> distanceFromCall(Call<S> i) {
		return Optional.of(0);
	}

	@Override
	protected Optional<Integer> distanceFromTest(Test<S> i) {
		Optional<Integer> s0 = distanceFromState(i.S0());
		Optional<Integer> s1 = distanceFromState(i.S1());
		return min(s0, s1);
	}

	private Optional<Integer> min(Optional<Integer> s0, Optional<Integer> s1) {
		if (s0.isPresent() && s1.isPresent()) {
			return Optional.of(Math.min(s0.get(), s1.get()));
		} else if (s0.isPresent()) {
			return s0;
		} else {
			return s1;
		}
	}
	
	public static <S> ControllerWrapper<S, S> wrapper() {
		return ShortestMinimumDistance<S>::new;
	}
}
