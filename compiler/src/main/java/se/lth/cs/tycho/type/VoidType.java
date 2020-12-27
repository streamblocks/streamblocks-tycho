package se.lth.cs.tycho.type;

public enum VoidType implements Type {
    INSTANCE;

    @Override
    public String toString() {
        return "void";
    }
}