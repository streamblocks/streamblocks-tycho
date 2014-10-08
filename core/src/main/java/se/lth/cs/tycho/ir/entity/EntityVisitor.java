package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.ir.entity.nl.NetworkDefinition;

public interface EntityVisitor<R, P> {
	public R visitActorMachine(ActorMachine node, P param);
	public R visitActor(Actor node, P param);
	public R visitNetworkDefinition(NetworkDefinition node, P param);
	public R visitGlobalEntityReference(GlobalEntityReference node, P param);
}
