package se.lth.cs.tycho.interp.values;

public interface Builder {
    void add(RefView r);

    Collection build();
}
