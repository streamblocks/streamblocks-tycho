package se.lth.cs.tycho.compiler;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

public final class UniqueNumbers implements LongSupplier {
    private long number;

    public UniqueNumbers() {
        number = 0;
    }

    public long next() {
        return number++;
    }

    public void reset() {
        number = 0;
    }

    @Override
    public long getAsLong() {
        return next();
    }
}
