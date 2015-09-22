package se.lth.cs.tycho.comp;

import java.util.concurrent.atomic.AtomicLong;

public final class UniqueNumbers {
	private AtomicLong number;
	public UniqueNumbers() {
		number = new AtomicLong(0);
	}
	public long next() {
		return number.getAndIncrement();
	}
}
