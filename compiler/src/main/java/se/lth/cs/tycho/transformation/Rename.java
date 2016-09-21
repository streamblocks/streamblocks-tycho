package se.lth.cs.tycho.transformation;

import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.decoration.VariableDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.stmt.StmtBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

public final class Rename {
	private Rename() {}

	public static <T extends IRNode> T renameVariables(T tree, Predicate<Tree<? extends VarDecl>> renameScope, LongSupplier uniqueNumbers) {
		return (T) Tree.of(tree).transformNodes(renameVariables(renameScope, uniqueNumbers));
	}

	public static Function<Tree<? extends IRNode>, IRNode> renameVariables(Predicate<Tree<? extends VarDecl>> renameScope, LongSupplier uniqueNumbers) {
		return new RenameVariables(renameScope, uniqueNumbers);
	}

	public static boolean isInputVariable(Tree<? extends VarDecl> varDecl) {
		return varDecl.parent().map(parent -> parent.node() instanceof InputPattern).orElse(false);
	}

	public static boolean isActorVariable(Tree<? extends VarDecl> varDecl) {
		return varDecl.parent().map(parent -> parent.node() instanceof CalActor).orElse(false);
	}

	public static boolean isActionVariable(Tree<? extends VarDecl> varDecl) {
		return varDecl.parent().map(parent -> parent.node() instanceof Action).orElse(false);
	}

	public static boolean isLocalVariable(Tree<? extends VarDecl> varDecl) {
		return varDecl.parent()
				.map(parent -> parent.node() instanceof ExprLet || parent.node() instanceof StmtBlock)
				.orElse(false);
	}

	private static final class RenameVariables implements Function<Tree<? extends IRNode>, IRNode> {
		private final LongSupplier uniqueNumbers;
		private final Map<Tree<? extends VarDecl>, String> nameTable;
		private final Predicate<Tree<? extends VarDecl>> renamePredicate;

		public RenameVariables(Predicate<Tree<? extends VarDecl>> renamePredicate, LongSupplier uniqueNumbers) {
			this.renamePredicate = renamePredicate;
			this.uniqueNumbers = uniqueNumbers;
			this.nameTable = new HashMap<>();
		}

		@Override
		public IRNode apply(Tree<? extends IRNode> treeNode) {
			IRNode node = treeNode.node();
			if (node instanceof VarDecl) {
				return renameDecl((Tree) treeNode);
			} else if (node instanceof Variable) {
				return renameVar((Tree) treeNode);
			} else if (node instanceof ValueParameter) {
				return renamePar((Tree) treeNode);
			} else if (node instanceof ExprGlobalVariable) {
				return renameGlobalVar((Tree) treeNode);
			} else {
				return node;
			}
		}

		private String name(Tree<? extends VarDecl> decl) {
			return nameTable.computeIfAbsent(decl, this::computeName);
		}

		private String computeName(Tree<? extends VarDecl> decl) {
			if (renamePredicate.test(decl)) {
				return decl.node().getOriginalName() + "_" + uniqueNumbers.getAsLong();
			} else {
				return decl.node().getName();
			}
		}

		private <D extends VarDecl<D>> D renameDecl(Tree<D> decl) {
			return decl.node().withName(name(decl));
		}

		private Variable renameVar(Tree<Variable> var) {
			return VariableDeclarations.getDeclaration(var)
					.map(decl -> var.node().withName(name(decl)))
					.orElse(var.node());
		}

		private ValueParameter renamePar(Tree<ValueParameter> par) {
			if (par.parent().get().node() instanceof TypeExpr) return par.node(); // TODO: remove this temporary workaround due to missing type declarations.
			return VariableDeclarations.getValueParameterDeclaration(par)
					.map(decl -> par.node().withName(name(decl)))
					.orElse(par.node());
		}

		private ExprGlobalVariable renameGlobalVar(Tree<ExprGlobalVariable> var) {
			return VariableDeclarations.getGlobalVariableDeclaration(var)
					.map(decl -> {
						String name = name(decl);
						QID namespace = var.node().getGlobalName().getButLast();
						QID globalName = namespace.concat(QID.of(name));
						return var.node().withGlobalName(globalName);
					}).orElse(var.node());
		}

	}
}
