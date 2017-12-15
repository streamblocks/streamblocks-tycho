package se.lth.cs.tycho.transformation.composition;

public class Connection {
	private final SourcePort source;
	private final TargetPort target;
	private final int bufferSize;

	public Connection(SourcePort source, TargetPort target, int bufferSize) {
		this.source = source;
		this.target = target;
		this.bufferSize = bufferSize;
	}

	public SourcePort getSource() {
		return source;
	}

	public TargetPort getTarget() {
		return target;
	}

	public int getBufferSize() {
		return bufferSize;
	}
}

