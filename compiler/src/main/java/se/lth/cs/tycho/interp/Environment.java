package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.ir.Variable;

import java.util.Set;

public interface Environment {
    Memory getMemory();

    Environment closure(int[] selectChannelIn, int[] selectChannelOut, Set<Variable> variables, Stack stack);

    Environment closure(Set<Variable> variables, Stack stack);
}
