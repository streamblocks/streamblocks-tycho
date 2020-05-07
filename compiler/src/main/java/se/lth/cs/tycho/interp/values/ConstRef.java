package se.lth.cs.tycho.interp.values;

public abstract class ConstRef implements RefView {
    private static final String wrongType = "Wrong type";

    private ConstRef() {
    }

    public static ConstRef of(Value v) {
        return new ConstValueRef(v);
    }

    public static ConstRef of(double d) {
        return new ConstDoubleRef(d);
    }

    public static ConstRef of(long l) {
        return new ConstLongRef(l);
    }

    public static ConstRef of(boolean b) {
        return new ConstBooleanRef(b);
    }

    public static ConstRef of(String s) {
        return new ConstStringRef(s);
    }

    private static class ConstValueRef extends ConstRef {
        private final Value v;

        public ConstValueRef(Value v) {
            this.v = v;
        }

        @Override
        public Value getValue() {
            return v;
        }

        @Override
        public void assignTo(Ref r) {
            r.setValue(v.copy());
        }
    }

    private static class ConstDoubleRef extends ConstRef {
        private final double d;

        public ConstDoubleRef(double d) {
            this.d = d;
        }

        @Override
        public double getDouble() {
            return d;
        }

        @Override
        public void assignTo(Ref r) {
            r.setDouble(d);
        }
    }

    private static class ConstLongRef extends ConstRef {
        private final long l;

        public ConstLongRef(long l) {
            this.l = l;
        }

        @Override
        public long getLong() {
            return l;
        }

        @Override
        public void assignTo(Ref r) {
            r.setLong(l);
        }

        @Override
        public String toString() {
            return String.valueOf(l);
        }
    }

    private static class ConstBooleanRef extends ConstRef {
        private final boolean b;

        public ConstBooleanRef(boolean b) {
            this.b = b;
        }

        @Override
        public boolean getBoolean() {
            return b;
        }

        @Override
        public void assignTo(Ref r) {
            r.setBoolean(b);
        }
    }

    private static class ConstStringRef extends ConstRef {
        private final String s;

        public ConstStringRef(String s) {
            this.s = s;
        }

        @Override
        public String getString() {
            return s;
        }

        @Override
        public void assignTo(Ref r) {
            r.setString(s);
        }
    }


    @Override
    public Value getValue() {
        throw new IllegalStateException(wrongType);
    }

    @Override
    public long getLong() {
        throw new IllegalStateException(wrongType);
    }

    @Override
    public double getDouble() {
        throw new IllegalStateException(wrongType);
    }

    @Override
    public boolean getBoolean() {
        throw new IllegalStateException(wrongType);
    }

    @Override
    public String getString() {
        throw new IllegalStateException(wrongType);
    }

    @Override
    public abstract void assignTo(Ref r);
}
