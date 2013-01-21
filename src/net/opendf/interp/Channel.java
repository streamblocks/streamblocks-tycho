package net.opendf.interp;

import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;

public interface Channel {
	public void peek(int i, Ref r);
	public void write(RefView r);
	public void remove(int n);
	public boolean tokens(int n);
	public boolean space(int n);
}
