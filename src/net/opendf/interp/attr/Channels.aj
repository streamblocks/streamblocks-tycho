package net.opendf.interp.attr;

import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.StmtOutput;

public aspect Channels {
	private interface ChannelId {}
	
	declare parents : (ExprInput || StmtOutput) implements ChannelId;
	
	private int ChannelId.channelId;
	
	public void ChannelId.setChanneID(int id) {
		channelId = id;
	}
	
	public int ChannelId.getChannelID() {
		return channelId;
	}

}
