package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.decoration.Namespaces;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.decoration.VariableDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprVariable;

import java.util.Optional;

public class ResolveGlobalVariableNamesPhase implements Phase {
	@Override
	public String getDescription() {
		return "Resolves local names of global variables to global names";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return (CompilationTask) Tree.of(task).transformNodes(this::resolve);
	}

	private IRNode resolve(Tree<? extends IRNode> tree) {
		return tree.tryCast(ExprVariable.class)
				.flatMap(this::getDeclaration)
				.flatMap(this::getGlobalName)
				.<IRNode> map(ExprGlobalVariable::new)
				.orElse(tree.node());
	}

	private Optional<Tree<VarDecl>> getDeclaration(Tree<ExprVariable> var) {
		return VariableDeclarations.getDeclaration(var.child(ExprVariable::getVariable));
	}

	private Optional<QID> getGlobalName(Tree<VarDecl> var) {
		return Namespaces.globalName(var);
	}
}
