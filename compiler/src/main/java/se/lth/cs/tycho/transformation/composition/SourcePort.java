package se.lth.cs.tycho.transformation.composition;

public class SourcePort extends PortRef {
	public SourcePort(int actor, String port) {
		super(actor, port, true);
	}
}
