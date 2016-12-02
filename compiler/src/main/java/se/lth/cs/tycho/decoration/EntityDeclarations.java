package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityReference;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.ir.network.Instance;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public final class EntityDeclarations {
	private static final Declarations declarations = MultiJ.instance(Declarations.class);
	private EntityDeclarations() {}

	public static Optional<Tree<GlobalEntityDecl>> getDeclaration(Tree<EntityReference> entityExprTree) {
		Optional<Tree<EntityReferenceGlobal>> global = entityExprTree.tryCast(EntityReferenceGlobal.class);
		if (global.isPresent()) {
			return getGlobalDeclaration(global.get());
		}
		Optional<Tree<EntityReferenceLocal>> local = entityExprTree.tryCast(EntityReferenceLocal.class);
		if (local.isPresent()) {
			return getLocalDeclaration(local.get());
		}
		throw new UnsupportedOperationException();
	}
	public static Optional<Tree<GlobalEntityDecl>> getLocalDeclaration(Tree<EntityReferenceLocal> reference) {
		Optional<Tree<? extends IRNode>> parent = reference.parent();
		while (parent.isPresent()) {
			List<Tree<GlobalEntityDecl>> decls = declarations.get(parent.get(), parent.get().node(), reference.node().getName())
					.collect(Collectors.toList());
			if (!decls.isEmpty()) return Optional.of(decls.get(0));
			parent = parent.get().parent();
		}
		return Optional.empty();
	}
	public static Optional<Tree<GlobalEntityDecl>> getGlobalDeclaration(Tree<EntityReferenceGlobal> reference) {
		QID ns = reference.node().getGlobalName().getButLast();
		String name = reference.node().getGlobalName().getLast().toString();
		return Namespaces.getNamespace(reference, ns)
				.flatMap(tree -> tree.children(NamespaceDecl::getEntityDecls))
				.filter(tree -> tree.node().getName().equals(name))
				.findFirst();
	}

	public static Optional<Tree<GlobalEntityDecl>> getInstanceDeclaration(Tree<Instance> instance) {
		QID ns = instance.node().getEntityName().getButLast();
		String name = instance.node().getEntityName().getLast().toString();
		return Namespaces.getNamespace(instance, ns)
				.flatMap(tree -> tree.children(NamespaceDecl::getEntityDecls))
				.filter(tree -> tree.node().getName().equals(name))
				.findFirst();
	}

	@Module
	interface Declarations {
		default Stream<Tree<GlobalEntityDecl>> get(Tree<?> tree, IRNode node, String name) {
			return Stream.empty();
		}

		default Stream<Tree<GlobalEntityDecl>> get(Tree<?> tree_, NamespaceDecl ns, String name) {
			Tree<NamespaceDecl> tree = tree_.assertNode(ns);
			Stream<Tree<GlobalEntityDecl>> local = tree.children(NamespaceDecl::getEntityDecls)
					.filter(decl -> decl.node().getName().equals(name));
			Stream<Tree<GlobalEntityDecl>> namespace = Namespaces.getNamespace(tree, ns.getQID())
					.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getEntityDecls))
					.filter(decl -> decl.node().getAvailability() != Availability.LOCAL)
					.filter(decl -> decl.node().getName().equals(name));
			Stream<Tree<GlobalEntityDecl>> imported = tree.children(NamespaceDecl::getImports)
					.flatMap(n -> imports(n, n.node(), name));
			return concat(local, concat(namespace, imported)).distinct();
		}

		Stream<Tree<GlobalEntityDecl>> imports(Tree<?> tree, Import imp, String name);

		default Stream<Tree<GlobalEntityDecl>> imports(Tree<?> tree, GroupImport imp, String name) {
			if (imp.getKind() == Import.Kind.ENTITY) {
				return Namespaces.getNamespace(tree, imp.getGlobalName())
						.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getEntityDecls))
						.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC)
						.filter(decl -> decl.node().getName().equals(name));
			} else {
				return Stream.empty();
			}
		}

		default Stream<Tree<GlobalEntityDecl>> imports(Tree<?> tree, SingleImport imp, String name) {
			if (imp.getKind() == Import.Kind.ENTITY && imp.getLocalName().equals(name)) {
				return Namespaces.getEntityDeclarations(tree, imp.getGlobalName())
						.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC);
			} else {
				return Stream.empty();
			}
		}

	}
}
