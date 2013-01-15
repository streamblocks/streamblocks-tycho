package net.opendf.interp.values;

import net.opendf.interp.Simulator;

public interface Procedure extends Value {
	public void exec(int args, Simulator sim);
}
