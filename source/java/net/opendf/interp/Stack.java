package net.opendf.interp;

import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;

public interface Stack {
	public Ref pop();

	public void remove(int n);

	public void push(RefView r);
	
	public Ref push();
	
	public void alloca(int n);

/**
 * @param i , 0 = top of stack
 * @return
 */
	public Ref peek(int i);

	public Ref closure(int select);

	public boolean isEmpty();

}
