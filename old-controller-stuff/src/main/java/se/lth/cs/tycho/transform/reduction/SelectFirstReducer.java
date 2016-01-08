package se.lth.cs.tycho.transform.reduction;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.transform.reduction.util.ControllerWrapper;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;
import se.lth.cs.tycho.transform.util.GenInstruction;


/**
 * State handler that filters the instructions by picking one at random.
 * @author gustav
 *
 * @param <S>
 */
public class SelectFirstReducer<S> extends FilteredController<S> {

	public SelectFirstReducer(Controller<S> stateHandler) {
		super(stateHandler);
	}
	
	@Override
	public List<GenInstruction<S>> instructions(S state) {
		List<GenInstruction<S>> instructions = original.instructions(state);
		List<GenInstruction<S>> selected = new ArrayList<>(1);
		selected.add(instructions.get(0));
		return selected;
	}

	public static <S> ControllerWrapper<S, S> wrapper() {
		return SelectFirstReducer<S>::new;
	}
}
