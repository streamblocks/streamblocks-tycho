package se.lth.cs.tycho.interp.values;

public interface List extends Value, Collection {
	public void get(int i, Ref r);
	public Ref getRef(int i);

	public void set(int i, RefView r);

	public int size();

	@Override
	public List copy();
}
