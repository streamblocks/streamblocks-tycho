package streamblocks.tycho.simulator;

import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.util.ImmutableList;
import streamblocks.tycho.simulator.values.Channel;

public interface Environment {

    public Memory getMemory();

    public Channel.InputEnd getSinkChannelInputEnd(int i);

    public Channel.InputEnd[] getSinkChannelInputEnds();

    public Channel.OutputEnd getSourceChannelOutputEnd(int i);

    public Channel.OutputEnd[] getSourceChannelOutputEnds();

    public Environment closure(int[] selectChannelIn, int[] selectChannelOut, ImmutableList<Variable> variables, Stack stack);


}
