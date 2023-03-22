package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.entity.cal.Action;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

/**
 * A qualified identifier is a sequence of names separated with the dot
 * character '.'. Each individual name in this sequence may contain any
 * character except '.'.
 * 
 * @author gustav
 *
 */
public final class QID extends AbstractIRNode{
	private final List<String> parts;


	public QID(List<String> parts) {
		this(null, parts);
	}

	private QID(QID original, List<String> parts) {
		super(original);
		this.parts = parts;
	}

	private QID part(int from, int to) {
		int count = getNameCount();
		if (from < 0 || from > count || to < from || to > count) {
			return null;
		} else {
			return new QID(parts.subList(from, to));
		}
	}

	/**
	 * Returns a QID consisting of the first name of this QID.
	 * 
	 * @return the first name of this QID or null if this QID is empty
	 */
	public QID getFirst() {
		return part(0, 1);
	}

	/**
	 * Returns a QID consisting of the last name of this QID.
	 * 
	 * @return the last name of this QID or null if this QID is empty
	 */
	public QID getLast() {
		int count = getNameCount();
		return part(count - 1, count);
	}

	/**
	 * Returns a QID consisting of the sequence of names in this QID except the
	 * first name.
	 * 
	 * @return the sequence of names except the first name in this QID or null
	 *         if this QID is empty
	 */
	public QID getButFirst() {
		return part(1, getNameCount());
	}

	/**
	 * Returns a QID consisting of the sequence of names in this QID except the
	 * last name.
	 * 
	 * @return the sequence of names except the last name in this QID or null if
	 *         this QID is empty
	 */
	public QID getButLast() {
		return part(0, getNameCount() - 1);
	}

	/**
	 * Returns a QID consisting of the name at the given position.
	 * 
	 * @param index
	 *            the position of the requested name
	 * @return the name at the given position
	 * @throws IndexOutOfBoundsException
	 *             if the given index is out of bounds.
	 */
	public QID getName(int index) {
		QID result = part(index, index + 1);
		if (result == null) {
			throw new IndexOutOfBoundsException();
		} else {
			return result;
		}
	}

	/**
	 * Returns the number of names in this QID.
	 * 
	 * @return the number of names in this QID
	 */
	public int getNameCount() {
		return parts.size();
	}

	/**
	 * Returns the sequence of names in this QID as a list of QIDs.
	 * 
	 * @return the names as a list
	 */
	public List<String> parts() {
		return Collections.unmodifiableList(parts);
	}

	/**
	 * Returns true if the first names of the given QID are equal to the names
	 * of this QID. Some examples: "a.b" is a prefix of "a.b.c" and a prefix of
	 * "a.b", but "a.b" is not a prefix of "a.bb", "a" or "b".
	 * 
	 * @param that
	 *            the QID to compare prefix with
	 * @return true if the given QID is a prefix of this QID
	 */
	public boolean isPrefixOf(QID that) {
		if (this.getNameCount() > that.getNameCount()) {
			return false;
		} else {
			for (int i = 0; i < this.getNameCount(); i++) {
				if (!this.parts.get(i).equals(that.parts.get(i))) {
					return false;
				}
			}
			return true;
		}
	}
	
	public QID getWithoutPrefix(QID prefix) {
		if (prefix.isPrefixOf(this)) {
			return part(prefix.getNameCount(), this.getNameCount());
		} else {
			return null;
		}
	}

	/**
	 * Returns a QID that is the concatenation of this QID and then the given
	 * QID.
	 * 
	 * @param qid
	 *            the QID to append to this QID
	 * @return a QID that is the concatenation of this QID and the given one.
	 */
	public QID concat(QID qid) {
		List<String> result = new ArrayList<>();
		result.addAll(parts);
		result.addAll(qid.parts);
		return new QID(result);
	}

	private static void assertNonEmptyNames(QID qid) {
		if (qid.parts.stream().anyMatch(String::isEmpty)) {
			throw new IllegalArgumentException("Empty names are not allowed");
		}
	}

	/**
	 * Parses the given name into a QID by splitting the name in each occurrence
	 * of '.'. The empty string yields the empty QID, but in all other cases are
	 * empty names illegal.
	 * 
	 * @param name a dot-separated qualified name
	 * @return a QID from the given name
	 * @throws IllegalArgumentException
	 *             if any of the names separated by dots are empty
	 */
	public static QID parse(String name) {
		if (name.isEmpty()) {
			return new QID(Collections.emptyList());
		}
		QID qid = new QID(Arrays.asList(name.split("\\.")));
		assertNonEmptyNames(qid);
		return qid;
	}

	/**
	 * Returns a QID from the given names. Empty names and names containing the
	 * character '.' are illegal.
	 * 
	 * @param names the sequence of names
	 * @return a QID consisting of the given names
	 * @throws IllegalArgumentException
	 *             if any of the names are empty or contains the '.' character.
	 */
	public static QID of(String... names) {
		if (Arrays.stream(names).anyMatch((s) -> s.contains("."))) {
			throw new IllegalArgumentException("Names may not contain the '.' character");
		} else {
			QID qid = new QID(new ArrayList<>(Arrays.asList(names)));
			assertNonEmptyNames(qid);
			return qid;
		}
	}

	/**
	 * Returns an empty QID.
	 * 
	 * @return an empty QID
	 */
	public static QID empty() {
		return new QID(Collections.emptyList());
	}
	
	/**
	 * Returns a Path with the same sequence of names as this QID. 
	 * @return a Path from this QID
	 * @throws java.nio.file.InvalidPathException if the QID can not be converted to a Path 
	 */
	public Path toPath() {
		return Paths.get("", parts.toArray(new String[parts.size()]));
	}

	/**
	 * Returns a string representation of this QID for which the following
	 * property holds if qid is not null:
	 * {@code QID.parse(qid.toString()).equals(qid);}
	 * 
	 * @return a String representation of this QID
	 */
	@Override
	public String toString() {
		return String.join(".", parts);
	}

	public String nameWithUnderscore() {
		return String.join("_", parts);
	}

	@Override
	public int hashCode() {
		return parts.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof QID) {
			return ((QID) o).parts.equals(parts);
		} else if (o == null) {
			throw new NullPointerException();
		} else {
			return false;
		}
	}

	public QID withLast(String newName) {
		List<String> newParts = new ArrayList<>(parts);
		newParts.set(newParts.size() - 1, newName);
		return new QID(newParts);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return null;
	}
}
