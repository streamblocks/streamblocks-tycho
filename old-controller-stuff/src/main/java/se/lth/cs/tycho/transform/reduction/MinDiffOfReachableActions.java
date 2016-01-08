package se.lth.cs.tycho.transform.reduction;

import se.lth.cs.tycho.transform.reduction.util.ControllerWrapper;
import se.lth.cs.tycho.transform.util.Controller;

/**
 * This reducer selects the test instruction with the largest difference between
 * the sets of reachable actions in the two branches.
 */
public class MinDiffOfReachableActions<S> extends MaxDiffOfReachableActions<S> {

	protected MinDiffOfReachableActions(Controller<S> original) {
		super(original);
	}
	@Override
	protected int extreme() {
		return Integer.MAX_VALUE;
	}
	@Override
	protected int cmp(int a, int b) {
		return -Integer.compare(a, b);
	}
	
	public static <S> ControllerWrapper<S, S> wrapper() {
		return MinDiffOfReachableActions<S>::new;
	}
}
