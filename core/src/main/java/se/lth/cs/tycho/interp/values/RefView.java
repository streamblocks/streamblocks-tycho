package se.lth.cs.tycho.interp.values;

public interface RefView {

	public Value getValue();

	public long getLong();

	public double getDouble();
	
	public String getString();

	public void assignTo(Ref r);

}