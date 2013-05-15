package net.opendf.transform.filter;

import net.opendf.transform.util.StateHandler;

public interface InstructionFilterFactory<S> {
	public StateHandler<S> createFilter(StateHandler<S> stateHandler);
}
