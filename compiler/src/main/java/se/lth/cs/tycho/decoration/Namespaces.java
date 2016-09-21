package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;

import java.util.Optional;
import java.util.stream.Stream;

public final class Namespaces {
	private Namespaces() {}

	public static Stream<Tree<NamespaceDecl>> getAllNamespaces(Tree<? extends IRNode> tree) {
		Optional<Tree<CompilationTask>> task = tree.tryCast(CompilationTask.class);
		if (!task.isPresent()) {
			task = tree.findParentOfType(CompilationTask.class);
		}
		if (task.isPresent()) {
			return task.get().children(CompilationTask::getSourceUnits).map(t -> t.child(SourceUnit::getTree));
		}
		Optional<Tree<NamespaceDecl>> nsDecl = tree.tryCast(NamespaceDecl.class);
		if (!nsDecl.isPresent()) {
			nsDecl = tree.findParentOfType(NamespaceDecl.class);
		}
		if (nsDecl.isPresent()) {
			return Stream.of(nsDecl.get());
		}
		return Stream.empty();
	}

	public static Stream<Tree<NamespaceDecl>> getNamespace(Tree<? extends IRNode> tree, QID namespace) {
		return getAllNamespaces(tree).filter(ns -> ns.node().getQID().equals(namespace));
	}

	public static Stream<Tree<GlobalVarDecl>> getVariableDeclarations(Tree<? extends IRNode> tree, QID globalName) {
		return getNamespace(tree, globalName.getButLast())
				.flatMap(ns -> ns.children(NamespaceDecl::getVarDecls))
				.filter(decl -> decl.node().getName().equals(globalName.getLast().toString()));
	}

	public static Stream<Tree<GlobalEntityDecl>> getEntityDeclarations(Tree<? extends IRNode> tree, QID globalName) {
		return getNamespace(tree, globalName.getButLast())
				.flatMap(ns -> ns.children(NamespaceDecl::getEntityDecls))
				.filter(decl -> decl.node().getName().equals(globalName.getLast().toString()));
	}

	public static Stream<Tree<GlobalTypeDecl>> getTypeDeclarations(Tree<? extends IRNode> tree, QID globalName) {
		return getNamespace(tree, globalName.getButLast())
				.flatMap(ns -> ns.children(NamespaceDecl::getTypeDecls))
				.filter(decl -> decl.node().getName().equals(globalName.getLast().toString()));
	}

	public static <D extends Decl> Optional<QID> globalName(Tree<D> decl) {
		return decl.parent()
				.flatMap(p -> p.tryCast(NamespaceDecl.class))
				.map(ns -> ns.node().getQID().concat(QID.of(decl.node().getName())));
	}
}
