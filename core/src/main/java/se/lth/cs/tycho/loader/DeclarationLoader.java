package se.lth.cs.tycho.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.DeclKind;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.MessageReporter;

/**
 * The declaration loader is responsible for loading global declarations and
 * keeping track of in which namespace declaration a declaration sits.
 * 
 * @author gustav
 *
 */
public class DeclarationLoader {
	private final Map<QID, List<GlobalDecl>> declCache = new HashMap<>();

	private final Map<Object, SourceCodeUnit> sourceCodeUnit = new IdentityHashMap<>();
	private final Map<Object, NamespaceDecl> enclosingNsDecl = new IdentityHashMap<>();

	private final Set<SourceCodeUnit> loaded = new HashSet<>();
	private final List<SourceCodeRepository> repositories = new ArrayList<>();
	private final MessageReporter messages;

	/**
	 * Constructs a new declaration loader that reports problems to the given
	 * message listener.
	 * 
	 * @param messages the message listener to report to
	 */
	public DeclarationLoader(MessageReporter messages) {
		this.messages = messages;
	}

	/**
	 * Adds a repository of source code units to this declaration loader. The
	 * repository is added if {@code repo.checkRepository(...) is successful. 
	 * 
	 * @param repo
	 *            the repository to add
	 * @return
	 */
	public boolean addRepository(SourceCodeRepository repo) {
		if (repo.checkRepository(messages)) {
			repositories.add(repo);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Loads the global entity declaration with the qualified name {@code qid}
	 * that is available from the namespace declaration {@code ns}.
	 * 
	 * @param qid
	 *            the name of the entity
	 * @param ns
	 *            the namespace declaration to where it is loaded
	 * @return the entity declaration
	 */
	public GlobalEntityDecl loadEntity(QID qid, NamespaceDecl ns) {
		return (GlobalEntityDecl) load(qid, DeclKind.ENTITY, ns);
	}

	/**
	 * Loads the global type declaration with the qualified name {@code qid}
	 * that is available from the namespace declaration {@code ns}.
	 * 
	 * @param qid
	 *            the name of the type
	 * @param ns
	 *            the namespace declaration to where it is loaded
	 * @return the type declaration
	 */
	public GlobalTypeDecl loadType(QID qid, NamespaceDecl ns) {
		return (GlobalTypeDecl) load(qid, DeclKind.TYPE, ns);
	}

	/**
	 * Loads the global variable declaration with the qualified name {@code qid}
	 * that is available from the namespace declaration {@code ns}.
	 * 
	 * @param qid
	 *            the name of the variable
	 * @param ns
	 *            the namespace declaration to where it is loaded
	 * @return the variable declaration
	 */
	public GlobalVarDecl loadVar(QID qid, NamespaceDecl ns) {
		return (GlobalVarDecl) load(qid, DeclKind.VAR, ns);
	}

	private GlobalDecl load(QID qid, DeclKind kind, NamespaceDecl ns) {
		loadUnits(qid, kind);
		return getFromCache(qid, kind, ns);
	}

	private GlobalDecl getFromCache(QID qid, DeclKind kind, NamespaceDecl ns) {
		List<GlobalDecl> candidates = declCache.getOrDefault(qid, Collections.emptyList())
				.stream()
				.filter(d -> d.getKind() == kind)
				.filter(d -> isAvailableFrom(d, ns))
				.collect(Collectors.toList());
		if (candidates.size() == 1) {
			return candidates.get(0);
		} else if (candidates.isEmpty()) {
			messages.report(Message.error(kind + " " + qid + " is not available"));
			return null;
		} else {
			String units = candidates.stream()
					.map(sourceCodeUnit::get)
					.distinct()
					.map(SourceCodeUnit::getLocationDescription)
					.collect(Collectors.joining("\n"));
			String message = "There are several definitions available for " + kind + " " + qid
					+ " in the following unit\n" + units;
			messages.report(Message.error(message));
			return null;
		}
	}

	private boolean isAvailableFrom(GlobalDecl d, NamespaceDecl ns) {
		switch (d.getAvailability()) {
		case PUBLIC:
			return true;
		case PRIVATE:
			return getQID(getLocation(d)).equals(getQID(ns));
		case LOCAL:
			return isEnclosed(getLocation(d), ns);
		}
		return false;
	}

	private boolean isEnclosed(NamespaceDecl enclosing, NamespaceDecl enclosed) {
		if (enclosing == enclosed) {
			return true;
		} else if (enclosed == null) {
			return false;
		} else {
			return isEnclosed(enclosing, getLocation(enclosed));
		}
	}

	private void loadUnits(QID qid, DeclKind kind) {
		repositories.stream()
				.flatMap(repo -> repo.findUnits(qid, kind).stream())
				.filter(unit -> !loaded.contains(unit))
				.forEach(unit -> {
					loaded.add(unit);
					NamespaceDecl ns = unit.load(messages);
					enclosingNsDecl.put(ns, null);
					populateCaches(ns, QID.empty(), unit);
				});
	}

	private void populateCaches(NamespaceDecl ns, QID parent, SourceCodeUnit unit) {
		sourceCodeUnit.put(ns, unit);
		QID qid = parent.concat(ns.getQID());
		for (NamespaceDecl child : ns.getNamespaceDecls()) {
			enclosingNsDecl.put(child, ns);
			populateCaches(child, qid, unit);
		}
		for (GlobalDecl decl : ns.getDecls()) {
			enclosingNsDecl.put(decl, ns);
			sourceCodeUnit.put(decl, unit);
			if (decl.getName() != null) {
				QID declQid = qid.concat(QID.of(decl.getName()));
				getDeclCacheEntry(declQid).add(decl);
			}
		}
	}

	private List<GlobalDecl> getDeclCacheEntry(QID qid) {
		if (!declCache.containsKey(qid)) {
			declCache.put(qid, new ArrayList<>());
		}
		return declCache.get(qid);
	}

	/**
	 * Returns the fully qualified name of the given global declaration
	 * 
	 * @param decl
	 *            the global declaration
	 * @return the fully qualified name
	 */
	public QID getQID(GlobalDecl decl) {
		return getQID(getLocation(decl)).concat(QID.of(decl.getName()));
	}

	/**
	 * Returns the fully qualified name of the given namespace declaration
	 * 
	 * @param decl
	 *            the namespace declaration
	 * @return the fully qualified name
	 */
	public QID getQID(NamespaceDecl decl) {
		if (decl == null) {
			return QID.empty();
		} else {
			return getQID(getLocation(decl)).concat(decl.getQID());
		}
	}

	/**
	 * Returns the location where the given global declaration sits
	 * 
	 * @param decl
	 *            the global declaration
	 * @return the location of the global declaration
	 */
	public NamespaceDecl getLocation(GlobalDecl decl) {
		return getEnclosingNsDecl(decl);
	}

	/**
	 * Returns the location where the given namespace declaration sits
	 * 
	 * @param decl
	 *            the namespace declaration
	 * @return the location of the namespace declaration
	 */
	public NamespaceDecl getLocation(NamespaceDecl decl) {
		return getEnclosingNsDecl(decl);
	}

	private NamespaceDecl getEnclosingNsDecl(Object decl) {
		if (enclosingNsDecl.containsKey(decl)) {
			return enclosingNsDecl.get(decl);
		}
		throw new IllegalArgumentException("The declaration is not loaded with this declaration loader.");
	}

}
