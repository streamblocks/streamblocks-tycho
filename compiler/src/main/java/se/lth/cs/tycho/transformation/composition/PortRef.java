package se.lth.cs.tycho.transformation.composition;

import java.util.Objects;

abstract class PortRef {
	private final int actor;
	private final String port;
	private final boolean isSourcePort;

	public PortRef(int actor, String port, boolean isSourcePort) {
		this.actor = actor;
		this.port = port;
		this.isSourcePort = isSourcePort;
	}

	public int getActor() {
		return actor;
	}

	public String getPort() {
		return port;
	}

	public boolean isSourcePort() {
		return isSourcePort;
	}

	public boolean isTargetPort() {
		return !isSourcePort;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof PortRef) {
			PortRef that = (PortRef) o;
			return this.actor == that.actor
					&& this.isSourcePort == that.isSourcePort
					&& Objects.equals(this.port, that.port);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actor, port, isSourcePort);
	}
}
