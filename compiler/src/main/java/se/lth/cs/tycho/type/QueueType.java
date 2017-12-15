package se.lth.cs.tycho.type;

import java.util.Objects;

public final class QueueType implements Type {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		QueueType queueType = (QueueType) o;
		return size == queueType.size &&
				Objects.equals(tokenType, queueType.tokenType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokenType, size);
	}
}
