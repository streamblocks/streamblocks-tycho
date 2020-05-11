package se.lth.cs.tycho.interp.values;

import se.lth.cs.tycho.interp.exception.InterpIndexOutOfBoundsException;

public interface List extends Value, Collection {
    void get(int i, Ref r) throws InterpIndexOutOfBoundsException;

    Ref getRef(int i) throws InterpIndexOutOfBoundsException;

    void set(int i, RefView r);

    int size();

    @Override
    List copy();
}
