package se.lth.cs.tycho.interp.values;

import se.lth.cs.tycho.interp.Interpreter;

public interface Function extends Value {
	
	/**
	 * Evaluate the body of the function and removes the argument from the stack.
	 * @param interpreter is used to evaluating the body.
	 * @return
	 */
	public RefView apply(Interpreter interpreter);
}
