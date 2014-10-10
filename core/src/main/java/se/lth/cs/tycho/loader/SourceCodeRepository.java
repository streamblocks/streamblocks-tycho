package se.lth.cs.tycho.loader;

import java.util.List;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.DeclKind;
import se.lth.cs.tycho.messages.MessageReporter;

/**
 * Repository of source code units.
 */
public interface SourceCodeRepository {
	/**
	 * Returns a list of all source code units that contains a daclaration for
	 * the specified qualified identifier of the specified kind. It is
	 * sufficient to return units that were present the last time
	 * {@code checkRepository} was called.
	 * 
	 * @param qid
	 *            the qualified identifier
	 * @param kind
	 *            the kind of declaration
	 * @return a list of source code units
	 */
	public List<SourceCodeUnit> findUnits(QID qid, DeclKind kind);

	/**
	 * Analyzes the repository and reports problems to the message listener such
	 * that successive calls to findUnits will not need to report any problems.
	 * Returns false if there is an error is reported, returns true otherwise.
	 * 
	 * @param messages
	 *            the message listener to report problems to
	 * @return true if the repository is in good condition
	 */
	public boolean checkRepository(MessageReporter messages);
}
