package se.lth.cs.tycho.transform.filter;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.ActorMachineState;


/**
 * State handler that filters the instructions by picking one at random.
 * @author gustav
 *
 * @param <S>
 */
public class SelectFirstInstruction<S> implements ActorMachineState<S> {

	private final ActorMachineState<S> actorMachineState;

	public SelectFirstInstruction(ActorMachineState<S> stateHandler) {
		this.actorMachineState = stateHandler;
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		List<GenInstruction<S>> instructions = actorMachineState.getInstructions(state);
		List<GenInstruction<S>> selected = new ArrayList<>(1);
		selected.add(instructions.get(0));
		return selected;
	}

	@Override
	public S initialState() {
		return actorMachineState.initialState();
	}
}
