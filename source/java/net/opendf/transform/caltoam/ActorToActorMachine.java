package net.opendf.transform.caltoam;

import java.util.List;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.transform.caltoam.ActorStates.State;
import net.opendf.transform.filter.InstructionFilterFactory;
import net.opendf.transform.util.ControllerGenerator;
import net.opendf.transform.util.StateHandler;

public class ActorToActorMachine {
	private final List<InstructionFilterFactory<State>> filters;

	public ActorToActorMachine(List<InstructionFilterFactory<State>> filters) {
		this.filters = filters;
	}

	private StateHandler<State> getStateHandler(StateHandler<State> innerHandler) {
		StateHandler<State> handler = innerHandler;
		for (InstructionFilterFactory<State> factory : filters) {
			handler = factory.createFilter(handler);
		}
		return handler;
	}
	
	public ActorMachine translate(Actor actor) {
		ActorToActorMachineHelper helper = new ActorToActorMachineHelper(actor);
		StateHandler<State> stateHandler = getStateHandler(helper.getActorStateHandler());
		ControllerGenerator<State> generator = ControllerGenerator.generate(stateHandler);
		int s = 0;
		for (State state : generator.getInterpretation()) {
			System.out.println("[" + s++ + "] " + state);
		}
		return new ActorMachine(
				helper.getInputPorts(),
				helper.getOutputPorts(),
				helper.getVarDecls(),
				generator.getController(),
				helper.getTransitions(),
				helper.getConditions());
	}
}
