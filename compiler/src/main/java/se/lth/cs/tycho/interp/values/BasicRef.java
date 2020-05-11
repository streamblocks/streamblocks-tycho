package se.lth.cs.tycho.interp.values;

import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;

public class BasicRef implements Ref {
    private static enum Type {
        VALUE, LONG, DOUBLE, STRING, BOOLEAN
    }

    private Type type;
    private Value value;
    private long long_;
    private double double_;
    private String string_;
    private boolean bool_;

    private void assertType(Type t) throws CompilationException {
        if (type != t) {
            String expectedType = t == null ? "UNINITIALIZED" : t.toString();
            String foundType = type == null ? "UNINITIALIZED" : type.toString();
            Diagnostic diagnostic = new Diagnostic(Diagnostic.Kind.ERROR, "Wrong type, expecting " + expectedType + " found " + foundType);
            throw new CompilationException(diagnostic);
        }
    }

    @Override
    public Value getValue() throws CompilationException {
        assertType(Type.VALUE);
        return value;
    }

    @Override
    public long getLong() throws CompilationException {
        assertType(Type.LONG);
        return long_;
    }

    @Override
    public double getDouble() throws CompilationException {
        assertType(Type.DOUBLE);
        return double_;
    }

    @Override
    public boolean getBoolean() throws CompilationException {
        assertType(Type.BOOLEAN);
        return bool_;
    }


    @Override
    public String getString() throws CompilationException {
        assertType(Type.STRING);
        return string_;
    }

    @Override
    public void setValue(Value v) {
        type = Type.VALUE;
        value = v;
    }

    @Override
    public void setLong(long v) {
        type = Type.LONG;
        long_ = v;
    }

    @Override
    public void setDouble(double v) {
        type = Type.DOUBLE;
        double_ = v;
    }

    @Override
    public void setBoolean(boolean v) {
        type = Type.BOOLEAN;
        bool_ = v;
    }

    @Override
    public void setString(String v) {
        type = Type.STRING;
        string_ = v;
    }

    @Override
    public void assignTo(Ref r) {
        if (type == null) {
            r.clear();
            return;
        }
        switch (type) {
            case LONG:
                r.setLong(long_);
                return;
            case VALUE:
                r.setValue(value.copy());
                return;
            case DOUBLE:
                r.setDouble(double_);
                return;
            case STRING:
                r.setString(string_);
                return;
        }
    }

    @Override
    public void clear() {
        type = null;
    }

    @Override
    public String toString() {
        if (type == null) {
            return "null";
        }
        switch (type) {
            case LONG:
                return Long.toString(long_);
            case VALUE:
                return value.toString();
            case DOUBLE:
                return Double.toString(double_);
            default:
                return "unknown";
        }
    }
}
