package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;

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
}
