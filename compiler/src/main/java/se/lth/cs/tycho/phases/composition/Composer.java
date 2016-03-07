package se.lth.cs.tycho.phases.composition;

import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Composer {
	private final List<ActorMachine> actorMachines;
	private final List<Connection> connections;

	public Composer(List<ActorMachine> actorMachines, List<Connection> connections) {
		this.actorMachines = actorMachines;
		this.connections = connections;
	}

	public ActorMachine compose() {
		return new ActorMachine(
				collectAll(ActorMachine::getInputPorts),
				collectAll(ActorMachine::getOutputPorts),
				collectAll(ActorMachine::getTypeParameters),
				collectAll(ActorMachine::getValueParameters),
				collectAll(ActorMachine::getScopes),
				new CompositionController(actorMachines, connections),
				collectAll(ActorMachine::getTransitions),
				collectAll(ActorMachine::getConditions));
	}

	private <E> ImmutableList<E> collectAll(Function<ActorMachine, List<E>> getList) {
		return actorMachines.stream()
				.map(getList)
				.flatMap(List::stream)
				.collect(ImmutableList.collector());
	}

}
