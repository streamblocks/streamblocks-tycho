package net.opendf.interp.values;

import net.opendf.interp.Interpreter;

public interface Procedure extends Value {
	public void exec(Interpreter interpreter);
}
