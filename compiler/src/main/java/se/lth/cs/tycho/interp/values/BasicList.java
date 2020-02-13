package se.lth.cs.tycho.interp.values;

import se.lth.cs.tycho.interp.exception.InterpIndexOutOfBoundsException;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;

import java.util.ArrayList;

public class BasicList implements List {
    private ArrayList<BasicRef> list;

    private BasicList(ArrayList<BasicRef> list) {
        this.list = list;
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    @Override
    public void get(int i, Ref r) throws InterpIndexOutOfBoundsException {
        if (i < 0 || i >= list.size()) {
            Diagnostic diagnostic = new Diagnostic(Diagnostic.Kind.ERROR, "Index=" + i + ", Size=" + list.size());
            throw new InterpIndexOutOfBoundsException(diagnostic);
        }
        list.get(i).assignTo(r);
    }

    @Override
    public Ref getRef(int i) throws InterpIndexOutOfBoundsException {
        if (i < 0 || i >= list.size()) {
            Diagnostic diagnostic = new Diagnostic(Diagnostic.Kind.ERROR, "Index=" + i + ", Size=" + list.size());
            throw new InterpIndexOutOfBoundsException(diagnostic);
        }
        return list.get(i);
    }

    @Override
    public void set(int i, RefView r) {
        r.assignTo(list.get(i));
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public List copy() {
        Builder b = new Builder();
        for (BasicRef r : list) b.add(r);
        return b.build();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        String sep = "";
        for (BasicRef v : list) {
            sb.append(sep);
            sep = ", ";
            sb.append(v.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    private class Iterator implements se.lth.cs.tycho.interp.values.Iterator {
        private int index = 0;

        @Override
        public Value getValue() throws CompilationException {
            return list.get(index).getValue();
        }

        @Override
        public long getLong() throws CompilationException {
            return list.get(index).getLong();
        }

        @Override
        public double getDouble() throws CompilationException {
            return list.get(index).getDouble();
        }

        @Override
        public String getString() throws CompilationException {
            return list.get(index).getString();
        }

        @Override
        public void assignTo(Ref r) {
            list.get(index).assignTo(r);
        }

        @Override
        public boolean finished() {
            return index >= list.size();
        }

        @Override
        public void advance() {
            index++;
        }
    }

    public static class Builder implements se.lth.cs.tycho.interp.values.Builder {

        private ArrayList<BasicRef> list = new ArrayList<BasicRef>();

        @Override
        public void add(RefView r) {
            BasicRef element = new BasicRef();
            r.assignTo(element);
            list.add(element);
        }

        @Override
        public BasicList build() {
            return new BasicList(list);
        }

    }
}
