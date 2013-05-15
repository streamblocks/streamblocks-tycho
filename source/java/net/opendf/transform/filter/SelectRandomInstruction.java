package net.opendf.transform.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.opendf.transform.util.GenInstruction;
import net.opendf.transform.util.StateHandler;


/**
 * State handler that filters the instructions by picking one at random.
 * @author gustav
 *
 * @param <S>
 */
public class SelectRandomInstruction<S> implements StateHandler<S> {

	private final Random random;
	private final StateHandler<S> stateHandler;

	public SelectRandomInstruction(StateHandler<S> stateHandler, Random random) {
		this.random = random;
		this.stateHandler = stateHandler;
	}

	public SelectRandomInstruction(StateHandler<S> stateHandler) {
		this(stateHandler, new Random());
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		List<GenInstruction<S>> instructions = stateHandler.getInstructions(state);
		List<GenInstruction<S>> selected = new ArrayList<>(1);
		selected.add(instructions.get(random.nextInt(instructions.size())));
		return selected;
	}

	@Override
	public S initialState() {
		return stateHandler.initialState();
	}
	
	public static <S> InstructionFilterFactory<S> getFactory(final Random random) {
		return new InstructionFilterFactory<S>() {
			public StateHandler<S> createFilter(StateHandler<S> stateHandler) {
				return new SelectRandomInstruction<S>(stateHandler, random);
			}
		};
	}
	
	public static <S> InstructionFilterFactory<S> getFactory() {
		return getFactory(new Random());
	}

}
