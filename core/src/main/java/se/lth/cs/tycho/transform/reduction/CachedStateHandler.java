package se.lth.cs.tycho.transform.reduction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.ActorMachineState;

public class CachedStateHandler<S> implements ActorMachineState<S> {
	private final ActorMachineState<S> actorMachineState;
	private final Map<S, List<GenInstruction<S>>> cache;

	public CachedStateHandler(ActorMachineState<S> stateHandler) {
		this.actorMachineState = stateHandler;
		this.cache = new HashMap<>();
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		if (cache.containsKey(state)) {
			return cache.get(state);
		} else {
			List<GenInstruction<S>> instrs = actorMachineState.getInstructions(state);
			cache.put(state, instrs);
			return instrs;
		}
	}

	@Override
	public S initialState() {
		return actorMachineState.initialState();
	}

}
