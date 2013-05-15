package net.opendf.transform.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.opendf.ir.am.Instruction;
import net.opendf.ir.util.ImmutableList;

public class ControllerGenerator<S> {
	private final ImmutableList<ImmutableList<Instruction>> controller;
	private final ImmutableList<S> interpretation;

	private ControllerGenerator(ImmutableList<ImmutableList<Instruction>> controller,
			ImmutableList<S> interpretation) {
		this.controller = controller;
		this.interpretation = interpretation;
	}

	/**
	 * @return the controller
	 */
	public ImmutableList<ImmutableList<Instruction>> getController() {
		return controller;
	}

	/**
	 * @return the interpretation
	 */
	public ImmutableList<S> getInterpretation() {
		return interpretation;
	}

	/**
	 * Generates a controller with its interpretation from a StateHandler.
	 * 
	 * @param stateHandler
	 * @return
	 */
	public static <S> ControllerGenerator<S> generate(StateHandler<S> stateHandler) {
		Map<S, Integer> states = new HashMap<>();
		Queue<S> queue = new LinkedList<>();
		List<List<GenInstruction<S>>> controller = new ArrayList<>();
		ImmutableList.Builder<S> interpretationBuilder = ImmutableList.builder();

		S initialState = stateHandler.initialState();
		queue.add(initialState);
		states.put(initialState, 0);

		while (!queue.isEmpty()) {
			assert states.size() == queue.size() + controller.size();

			S source = queue.poll();
			List<GenInstruction<S>> instructions = stateHandler.getInstructions(source);
			assert states.get(source).equals(controller.size());
			controller.add(instructions);
			interpretationBuilder.add(source);

			for (GenInstruction<S> instruction : instructions) {
				for (S destination : instruction.destinations()) {
					if (!states.containsKey(destination)) {
						states.put(destination, states.size());
						queue.add(destination);
					}
				}
			}
		}

		ImmutableList.Builder<ImmutableList<Instruction>> resultBuilder = ImmutableList.builder();
		for (List<GenInstruction<S>> instructions : controller) {
			ImmutableList.Builder<Instruction> stateBuilder = ImmutableList.builder();
			for (GenInstruction<S> instruction : instructions) {
				stateBuilder.add(instruction.generateInstruction(states));
			}
			ImmutableList<Instruction> state = stateBuilder.build();
			resultBuilder.add(state);
			assert instructions.size() == state.size();
		}
		ImmutableList<ImmutableList<Instruction>> result = resultBuilder.build();
		assert controller.size() == result.size();

		return new ControllerGenerator<S>(result, interpretationBuilder.build());
	}
}
