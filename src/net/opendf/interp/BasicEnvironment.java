package net.opendf.interp;

import net.opendf.interp.values.Ref;

public class BasicEnvironment implements Environment {
	private final BasicMemory memory;
	private final Channel[] channels;

	public BasicEnvironment(Channel[] channels, int memorySize) {
		this(channels, new BasicMemory(memorySize));
	}
	
	private BasicEnvironment(Channel[] channels, BasicMemory memory) {
		this.memory = memory;
		this.channels = channels;
	}

	@Override
	public BasicMemory getMemory() {
		return memory;
	}

	@Override
	public Channel getChannel(int i) {
		return channels[i];
	}

	@Override
	public BasicEnvironment closure(int[] selectChannels, int[] selectMemory, Ref[] addRefs) {
		BasicMemory mem = memory.closure(selectMemory, addRefs);
		Channel[] cs = new Channel[selectChannels.length];
		for (int i = 0; i < selectChannels.length; i++) {
			cs[i] = channels[selectChannels[i]];
		}
		return new BasicEnvironment(cs, mem);
	}

}
