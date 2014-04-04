package net.opendf.transform.caltoam;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.transform.caltoam.ActorStates.State;
import net.opendf.transform.util.ControllerGenerator;
import net.opendf.transform.util.StateHandler;

public class ActorToActorMachine {

	protected StateHandler<State> getStateHandler(StateHandler<State> handler) {
		return handler;
	}
	
	public final ActorMachine translate(Actor actor) {
		ActorToActorMachineHelper helper = new ActorToActorMachineHelper(actor);
		StateHandler<State> stateHandler = getStateHandler(helper.getActorStateHandler());
		ControllerGenerator<State> generator = ControllerGenerator.generate(stateHandler);
		return new ActorMachine(
				helper.getInputPorts(),
				helper.getOutputPorts(),
				helper.getScopes(),
				generator.getController(),
				helper.getTransitions(),
				helper.getConditions());
	}
}
