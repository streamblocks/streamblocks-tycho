package se.lth.cs.tycho.interp.values;

public interface Ref extends RefView{
    void setValue(Value v);

    void setLong(long v);

    void setDouble(double v);

    void setBoolean(boolean v);

    void setString(String v);

    void clear();
}
