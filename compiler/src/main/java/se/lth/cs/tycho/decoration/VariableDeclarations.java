package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.StarImport;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.expr.ExprComprehension;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
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
		Optional<Tree<VarDecl>> result = lookupInLexicalScope(variable);
		if (result.isPresent()) {
			return result;
		}
		Optional<Tree<VarDecl>> inNamespace = lookupInNamespace(variable);
		if (inNamespace.isPresent()) {
			return inNamespace;
		}
		Optional<Tree<VarDecl>> starImported = lookupInStarImports(variable);
		if (starImported.isPresent()) {
			return starImported;
		}
		Optional<Tree<VarDecl>> inPrelude = lookupInPrelude(variable);
		if (inPrelude.isPresent()) {
			return inPrelude;
		}
		return Optional.empty();
	}

	private static Optional<Tree<VarDecl>> lookupInPrelude(Tree<Variable> variable) {
		QID preludeQid = QID.of("prelude");
		return getNamespaceDecls(variable)
				.filter(namespaceTree -> namespaceTree.node().getQID().equals(preludeQid))
				.flatMap(namespaceTree -> namespaceTree.children(NamespaceDecl::getVarDecls))
				.filter(varTree -> varTree.node().getName().equals(variable.node().getName()))
				.findFirst();
	}

	public static Optional<Tree<VarDecl>> getValueParameterDeclaration(Tree<Parameter<Expression>> parameter) {
		Optional<Tree<? extends IRNode>> parent = parameter.parent();
		if (!parent.isPresent()) return Optional.empty();
		Optional<Tree<EntityInstanceExpr>> instanceExpr = parent.get().tryCast(EntityInstanceExpr.class);
		if (instanceExpr.isPresent()) {
			Optional<Tree<EntityDecl>> entityDecl = EntityDeclarations.getDeclaration(instanceExpr.get());
			if (!entityDecl.isPresent()) return Optional.empty();
			entityDecl = ImportDeclarations.followEntityImport(entityDecl.get());
			if (!entityDecl.isPresent()) return Optional.empty();
			return entityDecl.get().child(EntityDecl::getEntity).children(Entity::getValueParameters)
					.filter(tree -> tree.node().getName().equals(parameter.node().getName()))
					.findFirst();
		}
		return Optional.empty();
	}

	private static Optional<Tree<VarDecl>> lookupInStarImports(Tree<Variable> variable) {
		String name = variable.node().getName();
		Optional<Tree<NamespaceDecl>> ns = variable.findParentOfType(NamespaceDecl.class);
		List<Tree<NamespaceDecl>> namespaces = getNamespaceDecls(variable).collect(Collectors.toList());
		return ns.flatMap(namespace -> namespace.node().getStarImports().stream()
						.map(StarImport::getQID)
						.flatMap(imported -> namespaces.stream()
								.filter(n -> n.node().getQID().equals(imported))
								.flatMap(n -> n.children(NamespaceDecl::getVarDecls))
								.filter(varDecl -> varDecl.node().getName().equals(name))
								.filter(varDecl -> varDecl.node().getAvailability() == Availability.PUBLIC))
						.findFirst());
	}

	private static Optional<Tree<VarDecl>> lookupInNamespace(Tree<Variable> variable) {
		Optional<QID> namespace = getNamespace(variable);
		if (!namespace.isPresent()) {
			return Optional.empty();
		}
		QID qid = namespace.get();
		String name = variable.node().getName();
		return getNamespaceDecls(variable)
				.filter(ns -> ns.node().getQID().equals(qid))
				.flatMap(ns -> ns.children(NamespaceDecl::getVarDecls))
				.filter(varDecl -> varDecl.node().getName().equals(name))
				.filter(varDecl -> varDecl.node().getAvailability() != Availability.LOCAL)
				.findFirst();
	}

	private static Optional<QID> getNamespace(Tree<? extends IRNode> node) {
		return node.findParentOfType(NamespaceDecl.class).map(Tree::node).map(NamespaceDecl::getQID);
	}

	private static <A, B, C> Function<A, C> applySecond(BiFunction<A, B, C> func, B arg) {
		return a -> func.apply(a, arg);
	}

	private static <A, B, C> Function<B, C> applyFirst(BiFunction<A, B, C> func, A arg) {
		return b -> func.apply(arg, b);
	}

	private static Stream<Tree<NamespaceDecl>> getNamespaceDecls(Tree<? extends IRNode> node) {
		return node.findParentOfType(CompilationTask.class).map(Stream::of).orElse(Stream.empty())
				.flatMap(task -> task.children(CompilationTask::getSourceUnits))
				.map(unit -> unit.child(SourceUnit::getTree));
	}

	private static Optional<Tree<VarDecl>> lookupInLexicalScope(Tree<Variable> variable) {
		String name = variable.node().getName();
		return variable.parentChain()
					.map(tree -> getDeclarations(tree).filter(decl -> decl.node().getName().equals(name)).findFirst())
					.filter(Optional::isPresent)
					.map(Optional::get)
					.findFirst();
	}

	/**
	 * Returns the variable declarations of the given node, if that node declares any variables.
	 * Returns an empty list otherwise.
	 * @param scope a node
	 * @return the variables that the node declares
	 */
	public static Stream<Tree<VarDecl>> getDeclarations(Tree<? extends IRNode> scope) {
		return declarations.get(scope.node()).map(decl -> decl.attachTo(scope));
	}

	@Module
	interface Declarations {
		default Stream<Tree<VarDecl>> get(IRNode node) {
			return Stream.empty();
		}
		default Stream<Tree<VarDecl>> get(VarDecl decl) {
			return Stream.of(Tree.of(decl));
		}
		default Stream<Tree<VarDecl>> get(ExprLet let) {
			return Tree.of(let).children(ExprLet::getVarDecls);
		}
		default Stream<Tree<VarDecl>> get(ExprLambda lambda) {
			return Tree.of(lambda).children(ExprLambda::getValueParameters);
		}
		default Stream<Tree<VarDecl>> get(ExprProc proc) {
			return Tree.of(proc).children(ExprProc::getValueParameters);
		}
		default Stream<Tree<VarDecl>> get(ExprComprehension comprehension) {
			return Tree.of(comprehension).child(ExprComprehension::getGenerator).children(Generator::getVarDecls);
		}
		default Stream<Tree<VarDecl>> get(StmtBlock block) {
			return Tree.of(block).children(StmtBlock::getVarDecls);
		}
		default Stream<Tree<VarDecl>> get(StmtForeach foreach) {
			return Tree.of(foreach).child(StmtForeach::getGenerator).children(Generator::getVarDecls);
		}
		default Stream<Tree<VarDecl>> get(Action action) {
			return Stream.concat(
					Tree.of(action).children(Action::getVarDecls),
					Tree.of(action).children(Action::getInputPatterns)
							.flatMap(tree -> tree.children(InputPattern::getVariables)));
		}
		default Stream<Tree<VarDecl>> get(CalActor actor) {
			Stream<Tree<VarDecl>> varDecls = Tree.of(actor).children(CalActor::getVarDecls);
			Stream<Tree<VarDecl>> parameters = Tree.of(actor).children(CalActor::getValueParameters);
			return Stream.concat(varDecls, parameters);
		}
		default Stream<Tree<VarDecl>> get(ActorMachine actorMachine) {
			Tree<ActorMachine> root = Tree.of(actorMachine);
			Stream<Tree<VarDecl>> scopeVars = root.children(ActorMachine::getScopes)
					.flatMap(tree -> tree.children(Scope::getDeclarations));
			Stream<Tree<VarDecl>> parameters = root.children(ActorMachine::getValueParameters);
			return Stream.concat(scopeVars, parameters);
		}
		default Stream<Tree<VarDecl>> get(NamespaceDecl ns) {
			return Tree.of(ns).children(NamespaceDecl::getVarDecls);
		}
	}
}
