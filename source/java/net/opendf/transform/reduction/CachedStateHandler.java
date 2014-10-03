package net.opendf.transform.reduction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opendf.transform.util.GenInstruction;
import net.opendf.transform.util.StateHandler;

public class CachedStateHandler<S> implements StateHandler<S> {
	private final StateHandler<S> stateHandler;
	private final Map<S, List<GenInstruction<S>>> cache;

	public CachedStateHandler(StateHandler<S> stateHandler) {
		this.stateHandler = stateHandler;
		this.cache = new HashMap<>();
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		if (cache.containsKey(state)) {
			return cache.get(state);
		} else {
			List<GenInstruction<S>> instrs = stateHandler.getInstructions(state);
			cache.put(state, instrs);
			return instrs;
		}
	}

	@Override
	public S initialState() {
		return stateHandler.initialState();
	}

}
