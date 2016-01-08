package se.lth.cs.tycho.transform.reduction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.lth.cs.tycho.transform.reduction.util.ControllerWrapper;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;

/**
 * This reducer selects the test instruction with the largest difference between
 * the sets of reachable actions in the two branches.
 */
public class MaxDiffOfReachableActions<S> extends FilteredController<S> {

	protected MaxDiffOfReachableActions(Controller<S> original) {
		super(original);
		reachable = new HashMap<>();
	}
	
	private final Map<S, BitSet> reachable;

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		int branchXorSize = extreme();
		List<GenInstruction<S>> tests = new ArrayList<>();
		List<GenInstruction<S>> instructions = original.instructions(state);
		for (GenInstruction<S> i : instructions) {
			if (i.isTest()) {
				Test<S> test = i.asTest();
				BitSet diff = new BitSet();
				diff.or(reachable(test.S0()));
				diff.xor(reachable(test.S1()));
				int size = diff.cardinality();
				if (cmp(size, branchXorSize) == 0) {
					tests.add(i);
				} else if (cmp(size, branchXorSize) > 0) {
					tests.clear();
					tests.add(i);
					branchXorSize = size;
				}
			}
		}
		if (tests.isEmpty()) {
			return instructions;
		} else {
			return tests;
		}
	}
	
	protected int extreme() {
		return 0;
	}
	
	protected int cmp(int a, int b) {
		return Integer.compare(a, b);
	}

	private BitSet reachable(S state) {
		if (reachable.containsKey(state)) {
			return reachable.get(state);
		} else {
			BitSet result = instructions(state).stream().map(this::reachable).reduce(new BitSet(), this::union);
			reachable.put(state,  result);
			return result;
		}
	}

	private BitSet reachable(GenInstruction<S> i) {
		if (i.isTest()) {
			Test<S> test = i.asTest();
			BitSet s0 = reachable(test.S0());
			BitSet s1 = reachable(test.S1());
			return union(s0, s1);
		} else if (i.isCall()) {
			Call<S> call = i.asCall();
			BitSet result = new BitSet();
			result.set(call.T());
			return result;
		} else {
			return new BitSet();
		}
	}

	private BitSet union(BitSet a, BitSet b) {
		BitSet result = new BitSet();
		result.or(a);
		result.or(b);
		return result;
	}
	
	public static <S> ControllerWrapper<S, S> wrapper() {
		return MaxDiffOfReachableActions<S>::new;
	}

}
