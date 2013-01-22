package net.opendf.interp;

import java.util.Arrays;

import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;

public class FanOutChannel implements Channel {
	private Channel[] channels;
	
	public FanOutChannel() {
		this.channels = new Channel[0];
	}

	@Override
	public void peek(int i, Ref r) {
		throw wrongEnd();
	}

	@Override
	public void write(RefView r) {
		if (!space(1)) throw new IllegalStateException("Channel is full");
		for (Channel c : channels) {
			c.write(r);
		}
	}

	@Override
	public void remove(int n) {
		throw wrongEnd();
	}

	@Override
	public boolean tokens(int n) {
		throw wrongEnd();
	}

	@Override
	public boolean space(int n) {
		for (Channel c : channels) {
			if (!c.space(n)) return false;
		}
		return true;
	}
	
	public void addOutputEnd(Channel c) {
		final int n = channels.length;
		channels = Arrays.copyOf(channels, n+1);
		channels[n] = c;
	}
	
	private UnsupportedOperationException wrongEnd() {
		return new UnsupportedOperationException("Can not use input end as output end.");
	}

}
