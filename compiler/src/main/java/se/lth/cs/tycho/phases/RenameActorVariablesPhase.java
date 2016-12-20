package se.lth.cs.tycho.phases;


import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.transformation.Rename;

import java.util.function.Function;

public class RenameActorVariablesPhase implements Phase {
	@Override
	public String getDescription() {
		return "Renames actor and action variables.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Function<Tree<? extends IRNode>, IRNode> transformation =
				Rename.renameVariables(this::isActorOrActionVariable, context.getUniqueNumbers());
		return (CompilationTask) Tree.of(task).transformNodes(transformation);
	}

	private boolean isActorOrActionVariable(Tree<? extends VarDecl> decl) {
		return Rename.isActorVariable(decl) || Rename.isActionVariable(decl) || Rename.isInputVariable(decl);
	}

}
