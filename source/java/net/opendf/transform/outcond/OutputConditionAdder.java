package net.opendf.transform.outcond;

import net.opendf.ir.am.ActorMachine;
import net.opendf.transform.util.ControllerGenerator;

public class OutputConditionAdder {
	public static ActorMachine addOutputConditions(ActorMachine input) {
		OutputConditionStateHandler stateHandler = new OutputConditionStateHandler(input);
		ControllerGenerator<OutputConditionState> generator = ControllerGenerator.generate(stateHandler);
		return input.copy(
				input.getInputPorts(),
				input.getOutputPorts(),
				input.getVarDecls(),
				generator.getController(),
				input.getTransitions(),
				stateHandler.getConditions());
	}

}
