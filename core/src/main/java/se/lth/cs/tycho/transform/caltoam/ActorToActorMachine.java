package se.lth.cs.tycho.transform.caltoam;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.transform.caltoam.ActorStates.State;
import se.lth.cs.tycho.transform.util.ControllerGenerator;
import se.lth.cs.tycho.transform.util.StateHandler;

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
