package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.xdf.XDFNetwork;

public interface EntityVisitor<R, P> {
	default R visitEntity(Entity e, P p) {
		return e.accept(this, p);
	}
	R visitCalActor(CalActor entity, P param);
	R visitNlNetwork(NlNetwork entity, P param);
	R visitXDFNetwork(XDFNetwork entity, P param);
}
