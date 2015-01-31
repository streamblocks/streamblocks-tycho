package se.lth.cs.tycho.transform.reduction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;

public class CachedController<S> extends FilteredController<S> {
	private final Map<S, List<GenInstruction<S>>> cache;

	public CachedController(Controller<S> stateHandler) {
		super(stateHandler);
		this.cache = new HashMap<>();
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		if (cache.containsKey(state)) {
			return cache.get(state);
		} else {
			List<GenInstruction<S>> instrs = original.instructions(state);
			cache.put(state, instrs);
			return instrs;
		}
	}

}
