package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.exception.InterpIndexOutOfBoundsException;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.ir.Variable;

import java.util.Set;

public interface Memory {
    Ref get(Variable var) throws InterpIndexOutOfBoundsException;

    Ref declare(int scope, int offset);

    Memory closure(Set<Variable> variables, Stack stack);
}
