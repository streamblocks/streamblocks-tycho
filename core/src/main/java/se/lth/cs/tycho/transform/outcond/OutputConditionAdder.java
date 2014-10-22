package se.lth.cs.tycho.transform.outcond;

import java.util.List;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.transform.util.ActorMachineState;
import se.lth.cs.tycho.transform.util.ActorMachineState.Transformer;
import se.lth.cs.tycho.transform.util.ControllerGenerator;

public class OutputConditionAdder {
	public static ActorMachine addOutputConditions(ActorMachine input, List<Transformer<OutputConditionState, OutputConditionState>> stateTransformers) {
		OutputConditionStateHandler stateHandler = new OutputConditionStateHandler(input);
		ActorMachineState<OutputConditionState> transformed = stateHandler;
		for (Transformer<OutputConditionState, OutputConditionState> transformer : stateTransformers) {
			transformed= transformer.transform(stateHandler);
		}
		ControllerGenerator<OutputConditionState> generator = ControllerGenerator.generate(transformed);
		return input.copy(
				input.getInputPorts(),
				input.getOutputPorts(),
				input.getScopes(),
				generator.getController(),
				input.getTransitions(),
				stateHandler.getConditions());
	}

}
