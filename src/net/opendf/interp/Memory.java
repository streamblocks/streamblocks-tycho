package net.opendf.interp;

import net.opendf.interp.values.Ref;

public interface Memory {
	public Ref get(int index);
	
	public Ref declare(int index);
	
	public Memory closure(int[] select, Ref[] add);

}
