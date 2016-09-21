package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
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
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

	public static Optional<Tree<ParameterVarDecl>> getValueParameterDeclaration(Tree<ValueParameter> parameter) {
		Optional<Tree<? extends IRNode>> parent = parameter.parent();
		if (!parent.isPresent()) return Optional.empty();
		{
			Optional<Tree<EntityInstanceExpr>> instanceExpr = parent.get().tryCast(EntityInstanceExpr.class);
			if (instanceExpr.isPresent()) {
				Optional<Tree<GlobalEntityDecl>> entityDecl = EntityDeclarations.getDeclaration(instanceExpr.get().child(EntityInstanceExpr::getEntityName));
				if (!entityDecl.isPresent()) return Optional.empty();
				return entityDecl.get().child(GlobalEntityDecl::getEntity).children(Entity::getValueParameters)
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
				Optional<Tree<GlobalEntityDecl>> entityDecl = Namespaces.getNamespace(parameter, namespace)
						.flatMap(ns -> ns.children(NamespaceDecl::getEntityDecls))
						.filter(decl -> decl.node().getName().equals(name))
						.findFirst();
				if (!entityDecl.isPresent()) return Optional.empty();
				return entityDecl.get().child(GlobalEntityDecl::getEntity).children(Entity::getValueParameters)
						.filter(tree -> tree.node().getName().equals(parameter.node().getName()))
						.findFirst();
			}
		}
		return Optional.empty();
	}

	public static Optional<Tree<GlobalVarDecl>> getGlobalVariableDeclaration(Tree<ExprGlobalVariable> variable) {
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
			List<Tree<VarDecl>> result = declarations.get(parent.get(), parent.get().node(), name).collect(Collectors.toList());
			if (!result.isEmpty()) {
				return result;
			}
			parent = parent.get().parent();
		}
		return Collections.emptyList();
	}

	private static Predicate<Tree<? extends VarDecl>> hasName(String name) {
		return decl -> decl.node().getName().equals(name);
	}
	@Module
	interface Declarations {
		default Stream<Tree<VarDecl>> get(Tree<?> tree, IRNode node, String name) {
			return Stream.empty();
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprLet let, String name) {
			return tree.assertNode(let).children(ExprLet::getVarDecls).filter(hasName(name));
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprLambda lambda, String name) {
			return tree.assertNode(lambda).children(ExprLambda::getValueParameters).filter(hasName(name)).map(Tree::upCast);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprProc proc, String name) {
			return tree.assertNode(proc).children(ExprProc::getValueParameters).filter(hasName(name)).map(Tree::upCast);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprComprehension comprehension, String name) {
			return tree.assertNode(comprehension)
					.child(ExprComprehension::getGenerator)
					.children(Generator::getVarDecls)
					.filter(hasName(name));
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, StmtBlock block, String name) {
			return tree.assertNode(block).children(StmtBlock::getVarDecls).filter(hasName(name));
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, StmtForeach foreach, String name) {
			return tree.assertNode(foreach)
					.child(StmtForeach::getGenerator)
					.children(Generator::getVarDecls)
					.filter(hasName(name));
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, Action action, String name) {
			Tree<Action> actionTree = tree.assertNode(action);
			return Stream.<Tree<VarDecl>> concat(
					actionTree.children(Action::getVarDecls),
					actionTree.children(Action::getInputPatterns)
							.flatMap(t -> t.children(InputPattern::getVariables)).map(Tree::upCast)).filter(hasName(name));
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, CalActor actor, String name) {
			Stream<Tree<VarDecl>> varDecls = tree.assertNode(actor).children(CalActor::getVarDecls);
			Stream<Tree<VarDecl>> parameters = tree.assertNode(actor).children(CalActor::getValueParameters).map(Tree::upCast);
			return Stream.concat(varDecls, parameters).filter(hasName(name));
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ActorMachine actorMachine, String name) {
			Tree<ActorMachine> root = tree.assertNode(actorMachine);
			Stream<Tree<VarDecl>> scopeVars = root.children(ActorMachine::getScopes)
					.flatMap(t -> t.children(Scope::getDeclarations));
			Stream<Tree<VarDecl>> parameters = root.children(ActorMachine::getValueParameters).map(Tree::upCast);
			return Stream.concat(scopeVars, parameters).filter(hasName(name));
		}

		default Stream<Tree<VarDecl>> get(Tree<?> tree, NamespaceDecl ns, String name) {
			Tree<NamespaceDecl> nsTree = tree.assertNode(ns);
			Stream<Tree<GlobalVarDecl>> localDecls = nsTree.children(NamespaceDecl::getVarDecls)
					.filter(hasName(name));
			Stream<Tree<GlobalVarDecl>> nsDecls = Namespaces.getNamespace(nsTree, ns.getQID())
					.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getVarDecls))
					.filter(varDecl -> varDecl.node().getAvailability() != Availability.LOCAL)
					.filter(hasName(name));
			Stream<Tree<GlobalVarDecl>> imports = nsTree.children(NamespaceDecl::getImports)
					.flatMap(imp -> imports(imp, imp.node(), name));
			return Stream.concat(localDecls, Stream.concat(nsDecls, imports))
					.distinct()
					.map(Tree::upCast);
		}

		default Stream<Tree<VarDecl>> get(Tree<?> tree, CompilationTask task, String name) {
			return Namespaces.getNamespace(tree.assertNode(task), QID.of("prelude"))
					.flatMap(ns -> ns.children(NamespaceDecl::getVarDecls))
					.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC)
					.map(Tree::upCast);
		}

		Stream<Tree<GlobalVarDecl>> imports(Tree<?> tree, Import imp, String name);

		default Stream<Tree<GlobalVarDecl>> imports(Tree<?> tree, GroupImport imp, String name) {
			if (imp.getKind() == Import.Kind.VAR) {
				return Namespaces.getNamespace(tree.assertNode(imp), imp.getGlobalName())
						.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getVarDecls))
						.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC)
						.filter(hasName(name));
			} else {
				return Stream.empty();
			}
		}

		default Stream<Tree<GlobalVarDecl>> imports(Tree<?> tree, SingleImport imp, String name) {
			if (imp.getKind() == Import.Kind.VAR && imp.getLocalName().equals(name)) {
				return Namespaces.getNamespace(tree.assertNode(imp), imp.getGlobalName().getButLast())
						.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getVarDecls))
						.filter(decl -> decl.node().getName().equals(imp.getGlobalName().getLast().toString()))
						.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC);
			} else {
				return Stream.empty();
			}
		}

	}
}
