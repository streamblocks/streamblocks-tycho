package se.lth.cs.tycho.transform.filter;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.Controller;


/**
 * State handler that filters the instructions by picking one at random.
 * @author gustav
 *
 * @param <S>
 */
public class SelectFirstInstruction<S> implements Controller<S> {

	private final Controller<S> controller;

	public SelectFirstInstruction(Controller<S> stateHandler) {
		this.controller = stateHandler;
	}
	
	@Override
	public QID instanceId() {
		return controller.instanceId();
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		List<GenInstruction<S>> instructions = controller.instructions(state);
		List<GenInstruction<S>> selected = new ArrayList<>(1);
		selected.add(instructions.get(0));
		return selected;
	}

	@Override
	public S initialState() {
		return controller.initialState();
	}
	
	public static <S> Transformation<Controller<S>> transformation() {
		return (Controller<S> controller) -> new SelectFirstInstruction<>(controller);
	}
}
