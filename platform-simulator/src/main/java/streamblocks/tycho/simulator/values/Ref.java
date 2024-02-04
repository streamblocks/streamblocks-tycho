package streamblocks.tycho.simulator.values;


public interface Ref extends RefView{
    public void setValue(Value v);

    public void setLong(long v);

    public void setDouble(double v);

    public void setString(String v);

    public void clear();
}
