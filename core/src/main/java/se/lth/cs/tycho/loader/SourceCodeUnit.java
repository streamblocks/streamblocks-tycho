package se.lth.cs.tycho.loader;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.messages.MessageListener;

/**
 * Represents a source code unit that can be loaded into the program.
 */
public interface SourceCodeUnit {
	/**
	 * Returns a string that describes the location of this unit, e.g. a file
	 * system path or URL
	 * 
	 * @return the location of the unit
	 */
	public String getLocationDescription();

	/**
	 * Loads the source code unit and reports possible errors to the message
	 * listener. Returns the top level namespace declaration of the unit or null
	 * if an error occurred while loading.
	 * 
	 * @param listener
	 *            the listener to report problems to
	 * @return the top level namespace declaration of the unit
	 */
	public NamespaceDecl load(MessageListener listener);
}
