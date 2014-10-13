package se.lth.cs.tycho.instance;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.net.Network;

public interface InstanceVisitor<R, P> {
	public R visitActorMachine(ActorMachine instance, P param);
	public R visitNetwork(Network instance, P param);
}
