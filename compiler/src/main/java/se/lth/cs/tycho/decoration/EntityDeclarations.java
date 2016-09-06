package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityReference;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.ir.network.Instance;

import java.util.Optional;
import java.util.stream.Stream;

public final class EntityDeclarations {
	private static final Declarations declarations = MultiJ.instance(Declarations.class);
	private EntityDeclarations() {}

	public static Optional<Tree<EntityDecl>> getDeclaration(Tree<EntityReference> reference) {
		Optional<Tree<EntityReferenceGlobal>> global = reference.tryCast(EntityReferenceGlobal.class);
		if (global.isPresent()) {
			return getGlobalDeclaration(global.get());
		}
		Optional<Tree<EntityReferenceLocal>> local = reference.tryCast(EntityReferenceLocal.class);
		assert local.isPresent() : "Unknown subclass of EntityReference";
		return getLocalDeclaration(local.get());
	}
	public static Optional<Tree<EntityDecl>> getLocalDeclaration(Tree<EntityReferenceLocal> reference) {
		return reference.parentChain()
				.flatMap(tree -> declarations.get(tree.node()).map(t -> t.attachTo(tree)))
				.filter(tree -> tree.node().getName().equals(reference.node().getName()))
				.findFirst();
	}
	public static Optional<Tree<EntityDecl>> getGlobalDeclaration(Tree<EntityReferenceGlobal> reference) {
		QID ns = reference.node().getGlobalName().getButLast();
		String name = reference.node().getGlobalName().getLast().toString();
		return Namespaces.getNamespace(reference, ns)
				.flatMap(tree -> tree.children(NamespaceDecl::getEntityDecls))
				.filter(tree -> tree.node().getName().equals(name))
				.findFirst();
	}

	@Module
	interface Declarations {
		default Stream<Tree<EntityDecl>> get(IRNode node) {
			return Stream.empty();
		}

		default Stream<Tree<EntityDecl>> get(EntityDecl decl) {
			return Stream.of(Tree.of(decl));
		}

		default Stream<Tree<EntityDecl>> get(NamespaceDecl ns) {
			return Tree.of(ns).children(NamespaceDecl::getEntityDecls);
		}
	}
}
