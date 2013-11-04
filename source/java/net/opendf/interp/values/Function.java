package net.opendf.interp.values;

import net.opendf.interp.Interpreter;
import net.opendf.interp.exception.CALRuntimeException;

public interface Function extends Value {
	
	/**
	 * Evaluate the body of the function and removes the argument from the stack.
	 * @param interpreter is used to evaluating the body.
	 * @return
	 * @throws CALRuntimeException 
	 */
	public RefView apply(Interpreter interpreter) throws CALRuntimeException;
	
	/**
	 * @return the number of arguments this function takes, i.e. the number of values poped from the stack
	 */
	public int getNbrParameters();
}
