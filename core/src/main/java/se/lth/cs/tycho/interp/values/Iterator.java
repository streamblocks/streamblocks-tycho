package se.lth.cs.tycho.interp.values;

public interface Iterator extends RefView {
	public boolean finished();
	public void advance();
}
