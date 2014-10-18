package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.xdf.XDFNetwork;

public interface EntityVisitor<R, P> {
	public R visitCalActor(CalActor entity, P param);
	public R visitNlNetwork(NlNetwork entity, P param);
	public R visitXDFNetwork(XDFNetwork entity, P param);
}
