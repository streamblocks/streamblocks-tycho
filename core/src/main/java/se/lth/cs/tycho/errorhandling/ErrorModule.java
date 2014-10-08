package se.lth.cs.tycho.errorhandling;

import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.ir.IRNode;
/**
 * An ErrorModule is a collection of error and warning messages. Normally each step in the compilation chain implements ErrorModule. 
 * @author pera
 *
 */
public interface ErrorModule {
	/**
	 * @param msg the error message
	 * @param source indicate the source of the error. Used for printing the location of the error. May be null
	 */
	void error(String msg, IRNode source);

	/**
	 * @param msg the warning message
	 * @param source indicate the source of the error. Used for printing the location of the error. May be null
	 */
	void warning(String msg, IRNode position);
	
	/**
	 * Throws an CALCompiletimeException if an error has occurred else nothing is done.
	 * NOTE, nothing is printed and warnings are ignored.
	 * @throws CALCompiletimeException containing a reference to this ErrorModule
	 */
	public void abortIfError() throws CALCompiletimeException;
	
	/**
	 * Print all warning and error messages to System.err
	 */
	public void printMessages();
	public void printWarnings();
	public void printErrors();
	
	public boolean hasWarning();
	public boolean hasError();
	public boolean hasProblem();

}
