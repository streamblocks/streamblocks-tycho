package net.opendf.interp;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;

public class BasicEnvironment implements Environment {
	private final Memory memory;
	private final Channel.InputEnd[] sinkChannelsEnd;
	private final Channel.OutputEnd[] sourceChannelsEnd;

	public BasicEnvironment(Channel.InputEnd[] sinkChannelsEnd, Channel.OutputEnd[] sourceChannelsEnd, ActorMachine actorMachine) {
		this.memory = new BasicMemory(actorMachine);
		this.sinkChannelsEnd = sinkChannelsEnd;
		this.sourceChannelsEnd = sourceChannelsEnd;
	}
	public BasicEnvironment(Channel.InputEnd[] sinkChannelsEnds, Channel.OutputEnd[] sourceChannelsEnds, Memory mem) {
		this.memory = mem;
		this.sinkChannelsEnd = sinkChannelsEnds;
		this.sourceChannelsEnd = sourceChannelsEnds;
	}

	@Override
	public Memory getMemory() {
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
	public Environment closure(int[] selectChannelIn, int[] selectChannelOut, ImmutableList<Variable> variables, Stack stack){
		Memory mem = memory.closure(variables, stack);
		Channel.InputEnd[] csi = new Channel.InputEnd[selectChannelIn.length];
		Channel.OutputEnd[] cso = new Channel.OutputEnd[selectChannelOut.length];
		for (int i = 0; i < selectChannelIn.length; i++) {
			csi[i] = sinkChannelsEnd[selectChannelIn[i]];
		}
		for (int i = 0; i < selectChannelOut.length; i++) {
			cso[i] = sourceChannelsEnd[selectChannelOut[i]];
		}
		return new BasicEnvironment(csi, cso, mem);
	}

}
