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
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.DeclKind;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.messages.MessageReporter;

/**
 * The declaration loader is responsible for loading global declarations and
 * keeping track of in which namespace declaration a declaration sits.
 * 
 * @author gustav
 *
 */
public class DeclarationLoader {
	private final Map<QID, List<Decl>> declCache = new HashMap<>();

	private final Map<Object, SourceCodeUnit> sourceCodeUnit = new IdentityHashMap<>();
	private final Map<Object, NamespaceDecl> enclosingNsDecl = new IdentityHashMap<>();

	private final Set<SourceCodeUnit> loaded = new HashSet<>();
	private final List<SourceCodeRepository> repositories = new ArrayList<>();
	private final MessageReporter messages;

	/**
	 * Constructs a new declaration loader that reports problems to the given
	 * message listener.
	 * 
	 * @param messages
	 *            the message listener to report to
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
	public EntityDecl loadEntity(QID qid, NamespaceDecl ns) {
		return (EntityDecl) load(qid, DeclKind.ENTITY, ns);
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
	public TypeDecl loadType(QID qid, NamespaceDecl ns) {
		return (TypeDecl) load(qid, DeclKind.TYPE, ns);
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
	public VarDecl loadVar(QID qid, NamespaceDecl ns) {
		return (VarDecl) load(qid, DeclKind.VAR, ns);
	}

	private Decl load(QID qid, DeclKind kind, NamespaceDecl ns) {
		loadUnits(qid, kind);
		return getFromCache(qid, kind, ns);
	}

	private Decl getFromCache(QID qid, DeclKind kind, NamespaceDecl ns) {
		List<Decl> candidates = declCache.getOrDefault(qid, Collections.emptyList())
				.stream()
				.filter(d -> d.getDeclKind() == kind)
				.filter(d -> isAvailableFrom(d, ns))
				.collect(Collectors.toList());
		if (candidates.size() == 1) {
			return candidates.get(0);
		} else if (candidates.isEmpty()) {
			return null;
		} else {
			List<String> list = candidates.stream().map(sourceCodeUnit::get).map(SourceCodeUnit::getLocationDescription).collect(Collectors.toList());
			throw new AmbiguityException(kind, qid, list);
		}
	}

	private boolean isAvailableFrom(Decl d, NamespaceDecl ns) {
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
		for (Decl decl : ns.getAllDecls()) {
			enclosingNsDecl.put(decl, ns);
			sourceCodeUnit.put(decl, unit);
			if (decl.getName() != null) {
				QID declQid = qid.concat(QID.of(decl.getName()));
				getDeclCacheEntry(declQid).add(decl);
			}
		}
	}

	private List<Decl> getDeclCacheEntry(QID qid) {
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
	public QID getQID(Decl decl) {
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
	public NamespaceDecl getLocation(Decl decl) {
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
