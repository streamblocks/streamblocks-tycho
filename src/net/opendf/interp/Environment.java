package net.opendf.interp;

import net.opendf.interp.values.Ref;

public interface Environment {
	public Memory getMemory();
	public Channel getChannel(int i);
	
	public Environment closure(int[] selectChannels, int[] selectMemory, Ref[] addRefs);

}
