package net.opendf.transform.util;

import java.util.List;

public interface StateHandler<S> {
	public List<GenInstruction<S>> getInstructions(S state);
	public S initialState();
}
