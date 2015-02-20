package se.lth.cs.tycho.transform.reduction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;
import se.lth.cs.tycho.transform.util.GenInstruction;

/**
 * State handler that filters the instructions by picking one at random.
 * 
 * @author gustav
 *
 * @param <S>
 */
public class SelectRandomReducer<S> extends FilteredController<S> {

	private final Random random;

	public SelectRandomReducer(Controller<S> stateHandler, Random random) {
		super(stateHandler);
		this.random = random;
	}

	public SelectRandomReducer(Controller<S> stateHandler) {
		this(stateHandler, new Random());
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		List<GenInstruction<S>> instructions = original.instructions(state);
		List<GenInstruction<S>> selected = new ArrayList<>(1);
		selected.add(instructions.get(random.nextInt(instructions.size())));
		return selected;
	}
	
	public static <S> ControllerWrapper<S, S> wrapper(Random random) {
		return controller -> new SelectRandomReducer<>(controller, random);
	}
}
