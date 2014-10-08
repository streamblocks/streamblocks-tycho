package net.opendf.ir.entity;

import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.cal.Actor;
import net.opendf.ir.entity.nl.NetworkDefinition;

public interface EntityVisitor<R, P> {
	public R visitActorMachine(ActorMachine node, P param);
	public R visitActor(Actor node, P param);
	public R visitNetworkDefinition(NetworkDefinition node, P param);
	public R visitGlobalEntityReference(GlobalEntityReference node, P param);
}
