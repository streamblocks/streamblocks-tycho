package se.lth.cs.tycho.transform.outcond;

import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.transform.util.ControllerGenerator;

public class OutputConditionAdder {
	public static ActorMachine addOutputConditions(ActorMachine input) {
		OutputConditionStateHandler stateHandler = new OutputConditionStateHandler(input);
		ControllerGenerator<OutputConditionState> generator = ControllerGenerator.generate(stateHandler);
		return input.copy(
				input.getInputPorts(),
				input.getOutputPorts(),
				input.getScopes(),
				generator.getController(),
				input.getTransitions(),
				stateHandler.getConditions());
	}

}
