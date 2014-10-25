package se.lth.cs.tycho.transform.filter;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.Controller;

/**
 * State handler that filters the instructions by picking only Call instructions
 * if there is at least one.
 * 
 * @author gustav
 * 
 * @param <S>
 */
public class PrioritizeCallInstructions<S> implements Controller<S> {
	private final Controller<S> controller;

	public PrioritizeCallInstructions(Controller<S> stateHandler) {
		this.controller = stateHandler;
	}
	
	@Override
	public QID instanceId() {
		return controller.instanceId();
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		List<GenInstruction<S>> instructions = controller.instructions(state);
		for (GenInstruction<S> instr : instructions) {
			if (instr.isCall()) {
				return getCalls(instructions);
			}
		}
		return instructions;
	}

	private List<GenInstruction<S>> getCalls(List<GenInstruction<S>> instructions) {
		List<GenInstruction<S>> result = new ArrayList<>(1);
		for (GenInstruction<S> instr : instructions) {
			if (instr.isCall()) {
				result.add(instr);
			}
		}
		return result;
	}

	@Override
	public S initialState() {
		return controller.initialState();
	}
	
	public static <S> Transformation<Controller<S>> transformation() {
		return (Controller<S> controller) -> new PrioritizeCallInstructions<>(controller);
	}
}
