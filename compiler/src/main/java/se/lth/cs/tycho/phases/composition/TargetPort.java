package se.lth.cs.tycho.phases.composition;

public class TargetPort extends PortRef {
	public TargetPort(int actor, String port) {
		super(actor, port, false);
	}
}
