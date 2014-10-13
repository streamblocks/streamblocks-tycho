package se.lth.cs.tycho.transform.caltoam;

import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.transform.caltoam.ActorStates.State;
import se.lth.cs.tycho.transform.util.ControllerGenerator;
import se.lth.cs.tycho.transform.util.StateHandler;

public class ActorToActorMachine {
	
	private final List<StateHandler.FilterConstructor<State>> filterConstructors;
	
	public ActorToActorMachine() {
		this(Collections.emptyList());
	}
	public ActorToActorMachine(List<StateHandler.FilterConstructor<State>> filterCreators) {
		this.filterConstructors = filterCreators;
	}

	protected StateHandler<State> getStateHandler(StateHandler<State> handler) {
		for (StateHandler.FilterConstructor<State> creator : filterConstructors) {
			handler = creator.createStateFilter(handler);
		}
		return handler;
	}
	
	public final ActorMachine translate(CalActor calActor) {
		ActorToActorMachineHelper helper = new ActorToActorMachineHelper(calActor);
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
