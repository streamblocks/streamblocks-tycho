package net.opendf.interp;

import net.opendf.interp.values.Ref;

public interface Environment {
	public Memory getMemory();

	public Channel.InputEnd getChannelIn(int i);

	public Channel.OutputEnd getChannelOut(int i);

	public Environment closure(int[] selectChannelIn, int[] selectChannelOut, int[] selectMemory, Ref[] addRefs);

}
