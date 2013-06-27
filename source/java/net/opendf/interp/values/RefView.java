package net.opendf.interp.values;

public interface RefView {

	public Value getValue();

	public long getLong();

	public double getDouble();

	public void assignTo(Ref r);

}