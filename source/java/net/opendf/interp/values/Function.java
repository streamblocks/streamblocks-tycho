package net.opendf.interp.values;

import net.opendf.interp.Interpreter;

public interface Function extends Value {
	public RefView apply(Interpreter interpreter);
}
