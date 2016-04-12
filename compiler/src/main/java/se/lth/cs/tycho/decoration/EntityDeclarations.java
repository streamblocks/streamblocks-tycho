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

import java.util.Optional;
import java.util.stream.Stream;

public final class EntityDeclarations {
	private static final Declarations declarations = MultiJ.instance(Declarations.class);
	private EntityDeclarations() {}

	public static Optional<Tree<EntityDecl>> getDeclaration(Tree<EntityInstanceExpr> instance) {
		Optional<Tree<EntityDecl>> localDeclaration = instance.parentChain()
				.flatMap(tree -> declarations.get(tree.node()).map(t -> t.attachTo(tree)))
				.filter(tree -> tree.node().getName().equals(instance.node().getEntityName()))
				.findFirst();
		if (localDeclaration.isPresent()) {
			return localDeclaration;
		}
		Optional<Tree<CompilationTask>> task = instance.findParentOfType(CompilationTask.class);
		Optional<QID> ns = instance.findParentOfType(NamespaceDecl.class).map(Tree::node).map(NamespaceDecl::getQID);
		if (task.isPresent() && ns.isPresent()) {
			return task.get().children(CompilationTask::getSourceUnits)
					.map(tree -> tree.child(SourceUnit::getTree))
					.filter(tree -> tree.node().getQID().equals(ns.get()))
					.flatMap(tree -> tree.children(NamespaceDecl::getEntityDecls))
					.filter(tree -> tree.node().getName().equals(instance.node().getEntityName()))
					.findFirst();
		}
		return Optional.empty();
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
