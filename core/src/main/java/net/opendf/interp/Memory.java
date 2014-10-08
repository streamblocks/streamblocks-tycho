package net.opendf.interp;

import net.opendf.interp.values.Ref;
import net.opendf.ir.Variable;
import net.opendf.ir.util.ImmutableList;

public interface Memory {
	public Ref get(VariableLocation var);
	
	public Ref declare(int scope, int offset);
	
	public Memory closure(ImmutableList<Variable> variables, Stack stack);

}
