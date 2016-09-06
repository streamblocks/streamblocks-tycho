package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.expr.ExprComprehension;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VariableDeclarations {
	private static final Declarations declarations = MultiJ.instance(Declarations.class);

	private VariableDeclarations() {}

	/**
	 * Returns the declaration of the variable in the given context.
	 * @param variable the context of the variable whose definition is looked up
	 * @return the declaration of the varaible, if it is present in the tree.
	 */
	public static Optional<Tree<VarDecl>> getDeclaration(Tree<Variable> variable) {
		return first(getDeclarations(variable));
	}

	private static <T> Optional<T> first(List<T> list) {
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(list.get(0));
		}
	}

	public static Optional<Tree<VarDecl>> getValueParameterDeclaration(Tree<ValueParameter> parameter) {
		Optional<Tree<? extends IRNode>> parent = parameter.parent();
		if (!parent.isPresent()) return Optional.empty();
		{
			Optional<Tree<EntityInstanceExpr>> instanceExpr = parent.get().tryCast(EntityInstanceExpr.class);
			if (instanceExpr.isPresent()) {
				Optional<Tree<EntityDecl>> entityDecl = EntityDeclarations.getDeclaration(instanceExpr.get().child(EntityInstanceExpr::getEntityName));
				if (!entityDecl.isPresent()) return Optional.empty();
				return entityDecl.get().child(EntityDecl::getEntity).children(Entity::getValueParameters)
						.filter(tree -> tree.node().getName().equals(parameter.node().getName()))
						.findFirst();
			}
		}
		{
			Optional<Tree<Instance>> instance = parent.get().tryCast(Instance.class);
			if (instance.isPresent()) {
				QID entityName = instance.get().node().getEntityName();
				QID namespace = entityName.getButLast();
				String name = entityName.getLast().toString();
				Optional<Tree<EntityDecl>> entityDecl = Namespaces.getNamespace(parameter, namespace)
						.flatMap(ns -> ns.children(NamespaceDecl::getEntityDecls))
						.filter(decl -> decl.node().getName().equals(name))
						.findFirst();
				if (!entityDecl.isPresent()) return Optional.empty();
				return entityDecl.get().child(EntityDecl::getEntity).children(Entity::getValueParameters)
						.filter(tree -> tree.node().getName().equals(parameter.node().getName()))
						.findFirst();
			}
		}
		return Optional.empty();
	}

	public static Optional<Tree<VarDecl>> getGlobalVariableDeclaration(Tree<ExprGlobalVariable> variable) {
		QID namespace = variable.node().getGlobalName().getButLast();
		String name = variable.node().getGlobalName().getLast().toString();
		return Namespaces.getNamespace(variable, namespace)
				.flatMap(ns -> ns.children(NamespaceDecl::getVarDecls))
				.filter(decl -> decl.node().getName().equals(name))
				.findFirst();
	}

	private static List<Tree<VarDecl>> getDeclarations(Tree<Variable> variable) {
		String name = variable.node().getName();
		Optional<Tree<?>> parent = variable.parent();
		while (parent.isPresent()) {
			List<Tree<VarDecl>> result = declarations.get(parent.get(), parent.get().node())
					.filter(decl -> decl.node().getName().equals(name))
					.collect(Collectors.toList());
			if (!result.isEmpty()) {
				return result;
			}
			parent = parent.get().parent();
		}
		return Collections.emptyList();
	}

	@Module
	interface Declarations {
		default Stream<Tree<VarDecl>> get(Tree<?> tree, IRNode node) {
			return Stream.empty();
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprLet let) {
			return tree.assertNode(let).children(ExprLet::getVarDecls);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprLambda lambda) {
			return tree.assertNode(lambda).children(ExprLambda::getValueParameters);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprProc proc) {
			return tree.assertNode(proc).children(ExprProc::getValueParameters);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprComprehension comprehension) {
			return tree.assertNode(comprehension).child(ExprComprehension::getGenerator).children(Generator::getVarDecls);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, StmtBlock block) {
			return tree.assertNode(block).children(StmtBlock::getVarDecls);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, StmtForeach foreach) {
			return tree.assertNode(foreach).child(StmtForeach::getGenerator).children(Generator::getVarDecls);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, Action action) {
			Tree<Action> actionTree = tree.assertNode(action);
			return Stream.concat(
					actionTree.children(Action::getVarDecls),
					actionTree.children(Action::getInputPatterns)
							.flatMap(t -> t.children(InputPattern::getVariables)));
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, CalActor actor) {
			Stream<Tree<VarDecl>> varDecls = tree.assertNode(actor).children(CalActor::getVarDecls);
			Stream<Tree<VarDecl>> parameters = tree.assertNode(actor).children(CalActor::getValueParameters);
			return Stream.concat(varDecls, parameters);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ActorMachine actorMachine) {
			Tree<ActorMachine> root = tree.assertNode(actorMachine);
			Stream<Tree<VarDecl>> scopeVars = root.children(ActorMachine::getScopes)
					.flatMap(t -> t.children(Scope::getDeclarations));
			Stream<Tree<VarDecl>> parameters = root.children(ActorMachine::getValueParameters);
			return Stream.concat(scopeVars, parameters);
		}

		default Stream<Tree<VarDecl>> get(Tree<?> tree, NamespaceDecl ns) {
			Tree<NamespaceDecl> nsTree = tree.assertNode(ns);
			Stream<Tree<VarDecl>> localDecls = nsTree.children(NamespaceDecl::getVarDecls);
			Stream<Tree<VarDecl>> nsDecls = Namespaces.getNamespace(nsTree, ns.getQID())
					.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getVarDecls))
					.filter(varDecl -> varDecl.node().getAvailability() != Availability.LOCAL);
			Stream<Tree<VarDecl>> imports = nsTree.children(NamespaceDecl::getImports)
					.flatMap(imp -> imports(imp, imp.node()));
			return Stream.concat(localDecls, Stream.concat(nsDecls, imports)).distinct();
		}

		default Stream<Tree<VarDecl>> get(Tree<?> tree, CompilationTask task) {
			return Namespaces.getNamespace(tree.assertNode(task), QID.of("prelude"))
					.flatMap(ns -> ns.children(NamespaceDecl::getVarDecls))
					.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC);
		}

		Stream<Tree<VarDecl>> imports(Tree<?> tree, Import imp);

		default Stream<Tree<VarDecl>> imports(Tree<?> tree, GroupImport imp) {
			return Namespaces.getNamespace(tree.assertNode(imp), imp.getGlobalName())
					.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getVarDecls))
					.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC);
		}

		default Stream<Tree<VarDecl>> imports(Tree<?> tree, SingleImport imp) {
			return Namespaces.getNamespace(tree.assertNode(imp), imp.getGlobalName().getButLast())
					.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getVarDecls))
					.filter(decl -> decl.node().getName().equals(imp.getGlobalName().getLast().toString()))
					.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC);
		}

	}
}
