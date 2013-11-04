package net.opendf.interp.values;

import net.opendf.interp.exception.CALIndexOutOfBoundsException;

public interface List extends Value, Collection {
	public void get(int i, Ref r) throws CALIndexOutOfBoundsException;
	public Ref getRef(int i) throws CALIndexOutOfBoundsException;

	public void set(int i, RefView r);

	public int size();

	@Override
	public List copy();
}
