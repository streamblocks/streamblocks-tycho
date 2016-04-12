package se.lth.cs.tycho.comp;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

public final class UniqueNumbers implements LongSupplier {
	private AtomicLong number;
	public UniqueNumbers() {
		number = new AtomicLong(0);
	}
	public long next() {
		return number.getAndIncrement();
	}

	@Override
	public long getAsLong() {
		return next();
	}
}
