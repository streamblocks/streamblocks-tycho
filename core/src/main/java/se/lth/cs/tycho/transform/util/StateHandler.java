package se.lth.cs.tycho.transform.util;

import java.util.List;

public interface StateHandler<S> {
	public List<GenInstruction<S>> getInstructions(S state);
	public S initialState();
	
	public interface FilterConstructor<S> {
		public StateHandler<S> createStateFilter(StateHandler<S> original);
	}
}
