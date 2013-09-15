package net.opendf.interp;

import net.opendf.interp.values.Ref;
import net.opendf.ir.am.ActorMachine;

public class BasicEnvironment implements Environment {
	private final BasicMemory memory;
	private final Channel.InputEnd[] sinkChannelsEnd;
	private final Channel.OutputEnd[] sourceChannelsEnd;

	public BasicEnvironment(Channel.InputEnd[] sinkChannelsEnd, Channel.OutputEnd[] sourceChannelsEnd, ActorMachine actorMachine) {
		this.memory = new BasicMemory(actorMachine);
		this.sinkChannelsEnd = sinkChannelsEnd;
		this.sourceChannelsEnd = sourceChannelsEnd;
	}

	@Override
	public BasicMemory getMemory() {
		return memory;
	}

	@Override
	public Channel.InputEnd getSinkChannelInputEnd(int i) {
		return sinkChannelsEnd[i];
	}
	@Override
	public Channel.InputEnd[] getSinkChannelInputEnds() {
		return sinkChannelsEnd;
	}

	@Override
	public Channel.OutputEnd getSourceChannelOutputEnd(int i) {
		return sourceChannelsEnd[i];
	}

	@Override
	public Channel.OutputEnd[] getSourceChannelOutputEnds() {
		return sourceChannelsEnd;
	}

	@Override
	public BasicEnvironment closure(int[] selectChannelIn, int[] selectChannelOut, int[] selectMemory, Ref[] addRefs) {
		BasicMemory mem = memory.closure(selectMemory, addRefs);
		Channel.InputEnd[] csi = new Channel.InputEnd[selectChannelIn.length];
		Channel.OutputEnd[] cso = new Channel.OutputEnd[selectChannelOut.length];
		for (int i = 0; i < selectChannelIn.length; i++) {
			csi[i] = sinkChannelsEnd[selectChannelIn[i]];
		}
		for (int i = 0; i < selectChannelOut.length; i++) {
			cso[i] = sourceChannelsEnd[selectChannelOut[i]];
		}
		//FIXME, closure
		return this; //new BasicEnvironment(csi, cso, mem);
	}

}
