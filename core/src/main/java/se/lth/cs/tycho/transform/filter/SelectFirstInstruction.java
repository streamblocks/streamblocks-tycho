package se.lth.cs.tycho.transform.filter;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.StateHandler;


/**
 * State handler that filters the instructions by picking one at random.
 * @author gustav
 *
 * @param <S>
 */
public class SelectFirstInstruction<S> implements StateHandler<S> {

	private final StateHandler<S> stateHandler;

	public SelectFirstInstruction(StateHandler<S> stateHandler) {
		this.stateHandler = stateHandler;
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		List<GenInstruction<S>> instructions = stateHandler.getInstructions(state);
		List<GenInstruction<S>> selected = new ArrayList<>(1);
		selected.add(instructions.get(0));
		return selected;
	}

	@Override
	public S initialState() {
		return stateHandler.initialState();
	}
}
