package net.opendf.interp;

import net.opendf.interp.values.Ref;
import net.opendf.ir.common.Variable;

public interface Memory {
	public Ref get(Variable var);
	
	public Ref declare(int scope, int offset);
	
	public Memory closure(int[] select, Ref[] add);

}
