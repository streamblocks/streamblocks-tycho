package se.lth.cs.tycho.transform.caltoam;

import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.transform.caltoam.CalActorStates.State;
import se.lth.cs.tycho.transform.outcond.OutputConditionStateHandler;
import se.lth.cs.tycho.transform.reduction.ControllerWrapper;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.ControllerGenerator;

public class ActorToActorMachine {
	
	private final List<ControllerWrapper<CalActorStates.State, CalActorStates.State>> transformations;
	
	public ActorToActorMachine() {
		this(Collections.emptyList());
	}
	public ActorToActorMachine(List<ControllerWrapper<CalActorStates.State, CalActorStates.State>> filterCreators) {
		this.transformations = filterCreators;
	}

	protected Controller<CalActorStates.State> getStateHandler(Controller<CalActorStates.State> handler) {
		for (ControllerWrapper<CalActorStates.State, CalActorStates.State> creator : transformations) {
			handler = creator.wrap(handler);
		}
		return handler;
	}
	
	public final ActorMachine translate(CalActor calActor, NamespaceDecl location, QID instanceId) {
		ActorToActorMachineHelper helper = new ActorToActorMachineHelper(calActor, location, instanceId);
		Controller<State> stateHandler = getStateHandler(helper.getActorStateHandler());
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
