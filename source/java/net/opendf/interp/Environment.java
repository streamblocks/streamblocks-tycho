package net.opendf.interp;

import net.opendf.interp.values.Ref;

public interface Environment {
	public Memory getMemory();

	public Channel.InputEnd getSinkChannelInputEnd(int i);
	public Channel.InputEnd[] getSinkChannelInputEnds();

	public Channel.OutputEnd getSourceChannelOutputEnd(int i);
	public Channel.OutputEnd[] getSourceChannelOutputEnds();

	public Environment closure(int[] selectChannelIn, int[] selectChannelOut, int[] selectMemory, Ref[] addRefs);

}
