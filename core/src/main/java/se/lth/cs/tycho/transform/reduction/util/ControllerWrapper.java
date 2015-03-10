package se.lth.cs.tycho.transform.reduction.util;

import se.lth.cs.tycho.transform.util.Controller;

public interface ControllerWrapper<A, B> {
	public Controller<B> wrap(Controller<A> original);
}
