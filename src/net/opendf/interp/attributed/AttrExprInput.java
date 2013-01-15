package net.opendf.interp.attributed;

import net.opendf.ir.common.ExprInput;

public class AttrExprInput extends ExprInput implements ChannelId {
	private final int channel;
	
	public AttrExprInput(ExprInput base, int channel) {
		super(base.getPort(), base.getOffset(), base.getRepeat(), base.getPatternLength());
		this.channel = channel;
	}

	@Override
	public int channelId() {
		return channel;
	}

}
