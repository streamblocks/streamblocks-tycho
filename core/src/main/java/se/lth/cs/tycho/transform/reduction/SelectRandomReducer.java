package se.lth.cs.tycho.transform.reduction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.Controller;

/**
 * State handler that filters the instructions by picking one at random.
 * 
 * @author gustav
 *
 * @param <S>
 */
public class SelectRandomReducer<S> implements Controller<S> {

	private final Random random;
	private final Controller<S> controller;

	public SelectRandomReducer(Controller<S> stateHandler, Random random) {
		this.random = random;
		this.controller = stateHandler;
	}

	@Override
	public QID instanceId() {
		return controller.instanceId();
	}

	public SelectRandomReducer(Controller<S> stateHandler) {
		this(stateHandler, new Random());
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		List<GenInstruction<S>> instructions = controller.instructions(state);
		List<GenInstruction<S>> selected = new ArrayList<>(1);
		selected.add(instructions.get(random.nextInt(instructions.size())));
		return selected;
	}

	@Override
	public S initialState() {
		return controller.initialState();
	}
	
	public static <S> Transformation<Controller<S>> transformation(Random random) {
		return (Controller<S> controller) -> new SelectRandomReducer<>(controller, random);
	}
}
