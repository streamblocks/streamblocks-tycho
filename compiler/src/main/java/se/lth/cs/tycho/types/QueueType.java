package se.lth.cs.tycho.types;

public class QueueType implements Type {
	private final Type tokenType;
	private final int size;

	public QueueType(Type tokenType, int size) {
		this.tokenType = tokenType;
		this.size = size;
	}

	public Type getTokenType() {
		return tokenType;
	}

	public int getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "Queue(token:" + tokenType + ", size=" + size + ")";
	}
}
