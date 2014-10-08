package se.lth.cs.tycho.interp.values;


public interface Builder {
	public void add(RefView r);
	public Collection build();
}
