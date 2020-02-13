package se.lth.cs.tycho.interp.values;

public interface Iterator extends RefView{
    boolean finished();

    void advance();
}
