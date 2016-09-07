package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.decoration.EntityDeclarations;
import se.lth.cs.tycho.decoration.Namespaces;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.nl.EntityReference;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;

public class ResolveGlobalEntityNamesPhase implements Phase {
	@Override
	public String getDescription() {
		return "Resolves local entity names to global names.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return (CompilationTask) Tree.of(task).transformNodes(this::resolve);
	}

	private IRNode resolve(Tree<? extends IRNode> tree) {
		return tree.tryCast(EntityReferenceLocal.class)
				.flatMap(EntityDeclarations::getLocalDeclaration)
				.flatMap(Namespaces::globalName)
				.<IRNode> map(EntityReferenceGlobal::new)
				.orElse(tree.node());
	}
}
