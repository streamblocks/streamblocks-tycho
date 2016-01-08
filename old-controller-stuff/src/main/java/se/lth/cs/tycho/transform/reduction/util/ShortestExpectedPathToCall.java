package se.lth.cs.tycho.transform.reduction.util;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class ShortestExpectedPathToCall<S> extends ShortestPath<Double, S> {
	private final ProbabilityTable table;
	
	public ShortestExpectedPathToCall(Controller<S> controller, ProbabilityTable table) {
		super(controller, Comparator.naturalOrder());
		this.table = table;
	}

	@Override
	protected Optional<Double> distanceFromWait(Wait<S> wait) {
		return Optional.empty();
	}

	@Override
	protected Optional<Double> distanceFromCall(Call<S> call) {
		return Optional.of(0.0);
	}

	@Override
	protected Optional<Double> distanceFromTest(Test<S> test) {
		Optional<Double> s0 = distanceFromState(test.S0());
		Optional<Double> s1 = distanceFromState(test.S1());
		if (s0.isPresent() && s1.isPresent()) {
			double prob = table.probability(test.C());
			double expected = s0.get() * (1-prob) + s1.get() * prob;
			return Optional.of(expected + 1);
		} else {
			return Stream.of(s0, s1)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.findAny()
					.map(d -> d + 1);
		}
	}

}
