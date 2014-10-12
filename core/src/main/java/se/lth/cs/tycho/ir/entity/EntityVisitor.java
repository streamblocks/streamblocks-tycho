package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.ir.entity.nl.NetworkDefinition;
import se.lth.cs.tycho.ir.entity.xdf.XDF;

public interface EntityVisitor<R, P> {
	public R visitActor(Actor entity, P param);
	public R visitNetworkDefinition(NetworkDefinition entity, P param);
	public R visitGlobalEntityReference(GlobalEntityReference entity, P param);
	public R visitXDF(XDF entity, P param);
}
