package se.lth.cs.tycho.interp.values;

import se.lth.cs.tycho.interp.Interpreter;

public interface Procedure extends Value {
	public void exec(Interpreter interpreter);
}
