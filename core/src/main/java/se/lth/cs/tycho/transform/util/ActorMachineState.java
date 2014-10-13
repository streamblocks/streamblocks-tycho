package se.lth.cs.tycho.transform.util;

import java.util.List;

public interface ActorMachineState<S> {
	public List<GenInstruction<S>> getInstructions(S state);
	public S initialState();
	
	public interface Transformer<S, T> {
		public ActorMachineState<T> transform(ActorMachineState<S> original);
	}
}
