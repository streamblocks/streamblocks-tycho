package se.lth.cs.tycho.transform.reduction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.Controller;

public class CachedController<S> implements Controller<S> {
	private final Controller<S> controller;
	private final Map<S, List<GenInstruction<S>>> cache;

	public CachedController(Controller<S> stateHandler) {
		this.controller = stateHandler;
		this.cache = new HashMap<>();
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		if (cache.containsKey(state)) {
			return cache.get(state);
		} else {
			List<GenInstruction<S>> instrs = controller.instructions(state);
			cache.put(state, instrs);
			return instrs;
		}
	}

	@Override
	public S initialState() {
		return controller.initialState();
	}

	@Override
	public QID instanceId() {
		return controller.instanceId();
	}

}
