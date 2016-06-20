package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ImportDeclarations {
	public static Optional<Tree<VarDecl>> followVariableImport(Tree<VarDecl> decl) {
		return decl.node().isImport() ? followImport(decl, NamespaceDecl::getVarDecls) : Optional.of(decl);
	}

	public static Optional<Tree<EntityDecl>> followEntityImport(Tree<EntityDecl> decl) {
		return decl.node().isImport() ? followImport(decl, NamespaceDecl::getEntityDecls) : Optional.of(decl);
	}

	private static <D extends Decl> Optional<Tree<D>> followImport(Tree<D> decl, Function<NamespaceDecl, Collection<D>> getDecls) {
		Set<Tree<D>> visited = new HashSet<>();
		Optional<Tree<D>> result = Optional.of(decl);
		while (result.isPresent() && result.get().node().isImport()) {
			if (!visited.add(result.get())) {
				return Optional.empty();
			}
			QID qid = result.get().node().getQualifiedIdentifier();
			QID ns = qid.getButLast();
			String name = qid.getLast().toString();
			result = Namespaces.getNamespace(result.get(), ns)
					.flatMap(nsDecl -> nsDecl.children(getDecls))
					.filter(d -> d.node().getName().equals(name))
					.filter(d -> d.node().getAvailability() == Availability.PUBLIC)
					.findFirst();
		}
		return result;
	}
}
