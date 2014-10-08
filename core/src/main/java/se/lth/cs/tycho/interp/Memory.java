package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.util.ImmutableList;

public interface Memory {
	public Ref get(VariableLocation var);
	
	public Ref declare(int scope, int offset);
	
	public Memory closure(ImmutableList<Variable> variables, Stack stack);

}
