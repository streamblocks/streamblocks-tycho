package net.opendf.interp;

import net.opendf.interp.exception.CALIndexOutOfBoundsException;
import net.opendf.interp.values.Ref;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;

public interface Memory {
	public Ref get(VariableLocation var) throws CALIndexOutOfBoundsException;
	
	public Ref declare(int scope, int offset);
	
	public Memory closure(ImmutableList<Variable> variables, Stack stack);

}
