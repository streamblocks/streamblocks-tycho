package net.opendf.interp;

import net.opendf.interp.values.Ref;

public class BasicEnvironment implements Environment {
	private final BasicMemory memory;
	private final Channel.InputEnd[] channelIn;
	private final Channel.OutputEnd[] channelOut;

	public BasicEnvironment(Channel.InputEnd[] channelIn, Channel.OutputEnd[] channelOut, int memorySize) {
		this(channelIn, channelOut, new BasicMemory(memorySize));
	}

	private BasicEnvironment(Channel.InputEnd[] channelIn, Channel.OutputEnd[] channelOut, BasicMemory memory) {
		this.memory = memory;
		this.channelIn = channelIn;
		this.channelOut = channelOut;
	}

	@Override
	public BasicMemory getMemory() {
		return memory;
	}

	@Override
	public Channel.InputEnd getChannelIn(int i) {
		return channelIn[i];
	}

	@Override
	public Channel.OutputEnd getChannelOut(int i) {
		return channelOut[i];
	}

	@Override
	public BasicEnvironment closure(int[] selectChannelIn, int[] selectChannelOut, int[] selectMemory, Ref[] addRefs) {
		BasicMemory mem = memory.closure(selectMemory, addRefs);
		Channel.InputEnd[] csi = new Channel.InputEnd[selectChannelIn.length];
		Channel.OutputEnd[] cso = new Channel.OutputEnd[selectChannelIn.length];
		for (int i = 0; i < selectChannelIn.length; i++) {
			csi[i] = channelIn[selectChannelIn[i]];
		}
		for (int i = 0; i < selectChannelOut.length; i++) {
			cso[i] = channelOut[selectChannelOut[i]];
		}
		return new BasicEnvironment(csi, cso, mem);
	}

}
