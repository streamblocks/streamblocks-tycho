package se.lth.cs.tycho.interp.values;

public class Range implements Collection {
    private final long from;
    private final long to;

    public Range(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public Iterator iterator() {
        return new RangeIterator();
    }

    @Override
    public String toString(){
        return from + ".." + to;
    }

    private class RangeIterator implements Iterator {
        private long next;

        public RangeIterator() {
            next = from;
        }

        @Override
        public Value getValue() {
            throw new IllegalStateException("Wrong type");
        }

        @Override
        public long getLong() {
            if (finished()) {
                throw new IllegalStateException("End of iterator");
            }
            return next;
        }

        @Override
        public double getDouble() {
            throw new IllegalStateException("Wrong type");
        }

        @Override
        public String getString() {
            throw new IllegalStateException("Wrong type");
        }

        @Override
        public void assignTo(Ref r) {
            if (finished()) {
                throw new IllegalStateException("End of iterator");
            }
            r.setLong(next);
        }

        @Override
        public boolean finished() {
            return next > to;
        }

        @Override
        public void advance() {
            next += 1;
        }

        @Override
        public String toString(){
            return Long.toString(next);
        }
    }
}
