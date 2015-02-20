package se.lth.cs.tycho.transform.outcond;

import java.util.List;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.transform.reduction.ControllerWrapper;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.ControllerGenerator;

public class OutputConditionAdder {
	public static ActorMachine addOutputConditions(ActorMachine input, QID instanceId, List<ControllerWrapper<OutputConditionState, OutputConditionState>> stateTransformers) {
		OutputConditionStateHandler stateHandler = new OutputConditionStateHandler(input, instanceId);
		Controller<OutputConditionState> transformed = stateHandler;
		for (ControllerWrapper<OutputConditionState, OutputConditionState> transformer : stateTransformers) {
			transformed = transformer.wrap(stateHandler);
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
