package net.opendf.interp.values;

import net.opendf.interp.Interpreter;

public interface Function extends Value {
	
	/**
	 * Evaluate the body of the function and removes the argument from the stack.
	 * @param interpreter is used to evaluating the body.
	 * @return
	 */
	public RefView apply(Interpreter interpreter);
	
	/**
	 * @return the number of arguments this function takes, i.e. the number of values poped from the stack
	 */
	public int getNbrParameters();
}
