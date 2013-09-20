package net.opendf.interp;

import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;

public interface Environment {
	public Memory getMemory();

	public Channel.InputEnd getSinkChannelInputEnd(int i);
	public Channel.InputEnd[] getSinkChannelInputEnds();

	public Channel.OutputEnd getSourceChannelOutputEnd(int i);
	public Channel.OutputEnd[] getSourceChannelOutputEnds();

	public Environment closure(int[] selectChannelIn, int[] selectChannelOut, ImmutableList<Variable> variables, Stack stack);

}
