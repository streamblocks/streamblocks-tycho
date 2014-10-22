package se.lth.cs.tycho.transform.caltoam;

import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.transform.caltoam.ActorStates.State;
import se.lth.cs.tycho.transform.util.ControllerGenerator;
import se.lth.cs.tycho.transform.util.ActorMachineState;

public class ActorToActorMachine {
	
	private final List<ActorMachineState.Transformer<State, State>> transformers;
	
	public ActorToActorMachine() {
		this(Collections.emptyList());
	}
	public ActorToActorMachine(List<ActorMachineState.Transformer<State, State>> filterCreators) {
		this.transformers = filterCreators;
	}

	protected ActorMachineState<State> getStateHandler(ActorMachineState<State> handler) {
		for (ActorMachineState.Transformer<State, State> creator : transformers) {
			handler = creator.transform(handler);
		}
		return handler;
	}
	
	public final ActorMachine translate(CalActor calActor, NamespaceDecl location) {
		ActorToActorMachineHelper helper = new ActorToActorMachineHelper(calActor, location);
		ActorMachineState<State> stateHandler = getStateHandler(helper.getActorStateHandler());
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
