package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.ir.Variable;

import java.util.Set;

public class BasicEnvironment implements Environment {

    private final Memory memory;

    public BasicEnvironment(Memory memory) {
        this.memory = memory;
    }

    @Override
    public Memory getMemory() {
        return memory;
    }

    @Override
    public Environment closure(int[] selectChannelIn, int[] selectChannelOut, Set<Variable> variables, Stack stack) {
        return null;
    }

    @Override
    public Environment closure(Set<Variable> variables, Stack stack) {

        return null;
    }
}
