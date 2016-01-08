package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;

public interface EntityVisitor<R, P> {
	default R visitEntity(Entity e, P p) {
		return e.accept(this, p);
	}
	R visitCalActor(CalActor entity, P param);
	R visitNlNetwork(NlNetwork entity, P param);
	R visitActorMachine(ActorMachine entity, P param);
}
