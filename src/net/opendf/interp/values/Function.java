package net.opendf.interp.values;

import net.opendf.interp.Simulator;


public interface Function extends Value {
	public RefView apply(int args, Simulator sim);
}
