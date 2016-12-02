package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprComprehension;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class FreeVariables {
	private FreeVariables() {}

	private static final Collector module = MultiJ.instance(Collector.class);

	private static final Map<Tree<?>, Set<Tree<? extends VarDecl>>> cache = new HashMap<>();

	public static Set<Tree<? extends VarDecl>> freeVariables(Tree<? extends IRNode> node) {
		return cache.computeIfAbsent(node, n -> module.collect(n, n.node()));
	}

	@Module
	interface Collector {
		default Set<Tree<? extends VarDecl>> collect(Tree<?> tree, IRNode node) {
			return freeVariablesOfChildren(tree);
		}

		default Set<Tree<? extends VarDecl>> collect(Tree<?> tree, Variable var) {
			Optional<Tree<? extends VarDecl>> decl = VariableDeclarations.getDeclaration(tree.assertNode(var));
			if (decl.isPresent() && !(decl.get().node() instanceof GlobalVarDecl)) {
				return Collections.singleton(decl.get());
			} else {
				return Collections.emptySet();
			}
		}

		default Set<Tree<? extends VarDecl>> collect(Tree<?> tree, ExprLet expr) {
			Set<Tree<? extends VarDecl>> freeInChildren = freeVariablesOfChildren(tree);
			Set<Tree<LocalVarDecl>> decls = tree.assertNode(expr).children(ExprLet::getVarDecls).collect(Collectors.toSet());
			return difference(freeInChildren, decls);
		}

		default Set<Tree<? extends VarDecl>> collect(Tree<?> tree, ExprProc expr) {
			Tree<ExprProc> proc = tree.assertNode(expr);
			Set<Tree<? extends VarDecl>> result = new HashSet<>();
			proc.children(ExprProc::getBody).forEach(stmt -> result.addAll(freeVariables(stmt)));
			proc.children(ExprProc::getBody).forEach(stmt ->
					result.addAll(freeVariables(stmt)));
			proc.children(ExprProc::getValueParameters).forEach(result::remove);
			proc.children(ExprProc::getClosure).forEach(result::remove);
			return result;
		}

		default Set<Tree<? extends VarDecl>> collect(Tree<?> tree, ExprLambda expr) {
			Tree<ExprLambda> lambda = tree.assertNode(expr);
			Set<Tree<? extends VarDecl>> result = new HashSet<>();
			Tree<Expression> body = lambda.child(ExprLambda::getBody);
			result.addAll(freeVariables(body));
			lambda.children(ExprLambda::getValueParameters).forEach(result::remove);
			lambda.children(ExprLambda::getClosure).forEach(result::remove);
			return result;
		}

		default Set<Tree<? extends VarDecl>> collect(Tree<?> tree, ExprComprehension expr) {
			Set<Tree<? extends VarDecl>> decls = tree.assertNode(expr)
					.child(ExprComprehension::getGenerator)
					.children(Generator::getVarDecls)
					.collect(Collectors.toSet());
			return difference(freeVariablesOfChildren(tree), decls);
		}

		default Set<Tree<? extends VarDecl>> collect(Tree<?> tree, StmtBlock block) {
			return difference(freeVariablesOfChildren(tree), tree.assertNode(block).children(StmtBlock::getVarDecls).collect(Collectors.toSet()));
		}

		default Set<Tree<? extends VarDecl>> collect(Tree<?> tree, StmtForeach stmt) {
			Set<? extends Tree<? extends VarDecl>> varDecls = tree.assertNode(stmt)
					.child(StmtForeach::getGenerator)
					.children(Generator::getVarDecls)
					.collect(Collectors.toSet());
			return difference(freeVariablesOfChildren(tree), varDecls);
		}

		default Set<Tree<? extends VarDecl>> difference(Set<? extends Tree<? extends VarDecl>> decls, Set<? extends Tree<? extends VarDecl>> except) {
			Set<Tree<? extends VarDecl>> set = new HashSet<>(decls);
			set.removeAll(except);
			return set;
		}

		default Set<Tree<? extends VarDecl>> freeVariablesOfChildren(Tree<?> tree) {
			return tree.children()
					.flatMap(child -> freeVariables(child).stream())
					.collect(Collectors.toSet());
		}
	}
}
