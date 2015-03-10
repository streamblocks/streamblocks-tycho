package se.lth.cs.tycho.transform.reduction;

import java.util.Optional;

import se.lth.cs.tycho.transform.reduction.util.ControllerWrapper;
import se.lth.cs.tycho.transform.reduction.util.Rational;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class ShortestExpectedDistance<S> extends ShortestDistance<S, Rational> {

	public ShortestExpectedDistance(Controller<S> original) {
		super(original);
	}

	@Override
	protected Optional<Rational> distanceFromWait(Wait<S> i) {
		return Optional.empty();
	}

	@Override
	protected Optional<Rational> distanceFromCall(Call<S> i) {
		return Optional.of(Rational.valueOf(0));
	}

	@Override
	protected Optional<Rational> distanceFromTest(Test<S> i) {
		Optional<Rational> s0 = distanceFromState(i.S0());
		Optional<Rational> s1 = distanceFromState(i.S1());
		return average(s0, s1);
	}

	private Optional<Rational> average(Optional<Rational> s0, Optional<Rational> s1) {
		if (s0.isPresent() && s1.isPresent()) {
			Rational avg = s0.get().add(s1.get()).divide(Rational.valueOf(2));
			return Optional.of(avg);
		} else if (s0.isPresent()) {
			return s0;
		} else {
			return s1;
		}
	}
	
	public static <S> ControllerWrapper<S, S> wrapper() {
		return ShortestExpectedDistance<S>::new;
	}
}
