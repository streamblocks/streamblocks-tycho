package net.opendf.interp.attr;

import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.am.PortCondition;

public aspect Channels {
	private interface ChannelId {}
	
	declare parents : (ExprInput || StmtOutput || PortCondition) implements ChannelId;
	
	private int ChannelId.channelId;
	
	public void ChannelId.setChannelId(int id) {
		channelId = id;
	}
	
	public int ChannelId.getChannelId() {
		return channelId;
	}

}
