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
import se.lth.cs.tycho.ir.decl.*;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprComprehension;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VariableDeclarations {
	private static final Declarations declarations = MultiJ.instance(Declarations.class);
	private static final ImplicitDeclarations implicitDeclarations = MultiJ.instance(ImplicitDeclarations.class);
	private static final Imports imports = MultiJ.instance(Imports.class);

	private VariableDeclarations() {}

	/**
	 * Returns the declaration of the variable in the given context.
	 * @param variable the context of the variable whose definition is looked up
	 * @return the declaration of the varaible, if it is present in the tree.
	 */
	public static Optional<Tree<? extends VarDecl>> getDeclaration(Tree<Variable> variable) {
		return first(getDeclarations(variable));
	}

	private static <T> Optional<T> first(List<T> list) {
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(list.get(0));
		}
	}

	private static List<Tree<? extends VarDecl>> getDeclarations(Tree<Variable> variable) {
		String name = variable.node().getName();
		Optional<Tree<?>> parent = variable.parent();
		while (parent.isPresent()) {
			List<Tree<VarDecl>> result = getEnvironment(parent.get()).get(name);
			if (result != null) {
				return (List) result;
			}
			parent = parent.get().parent();
		}
		return Collections.emptyList();
	}

	private static Map<String, List<Tree<VarDecl>>> getEnvironment(Tree<?> tree) {
		return environments.computeIfAbsent(tree, VariableDeclarations::buildEnvironment);
	}

	private static final Map<Tree<?>, Map<String, List<Tree<VarDecl>>>> environments = new HashMap<>();

	private static Map<String, List<Tree<VarDecl>>> buildEnvironment(Tree<?> tree) {
		Map<String, List<Tree<VarDecl>>> env = new HashMap<>();
		Stream<Tree<VarDecl>> explicit = declarations.get(tree, tree.node());
		Stream<Tree<VarDecl>> implicit = implicitDeclarations.get(tree, tree.node()).map(Tree::upCast);
		Stream.concat(explicit, implicit).distinct().forEach(decl -> addDecl(env, decl.node().getName(), decl));
		imports.singleImports(tree, tree.node())
				.filter(imp -> imp.node().getKind() == Import.Kind.VAR)
				.forEach(imp -> Namespaces.getVariableDeclarations(imp, imp.node().getGlobalName())
						.forEach(decl -> addDecl(env, imp.node().getLocalName(), Tree.upCast(decl))));
		imports.groupImports(tree, tree.node())
				.filter(imp -> imp.node().getKind() == Import.Kind.VAR)
				.forEach(imp -> Namespaces.getNamespace(imp, imp.node().getGlobalName())
						.flatMap(ns -> ns.children(NamespaceDecl::getVarDecls))
						.forEach(decl -> {
							String name = decl.node().getName();
							if (!env.containsKey(name)) {
								env.put(name, Collections.singletonList(Tree.upCast(decl)));
							}
						}));
		return env;
	}

	private static void addDecl(Map<String, List<Tree<VarDecl>>> env, String name, Tree<VarDecl> decl) {
		env.computeIfAbsent(name, x -> new ArrayList<>())
				.add(decl);
	}

	@Module
	interface Imports {
		default Stream<Tree<SingleImport>> singleImports(Tree<?> tree, IRNode node) {
			return Stream.empty();
		}

		default Stream<Tree<SingleImport>> singleImports(Tree<?> tree, NamespaceDecl ns) {
			return tree.assertNode(ns)
					.children(NamespaceDecl::getImports)
					.map(i -> i.tryCast(SingleImport.class))
					.flatMap(this::optionalToStream);
		}

		default Stream<Tree<GroupImport>> groupImports(Tree<?> tree, IRNode node) {
			return Stream.empty();
		}

		default Stream<Tree<GroupImport>> groupImports(Tree<?> tree, NamespaceDecl ns) {
			return tree.assertNode(ns)
					.children(NamespaceDecl::getImports)
					.map(i -> i.tryCast(GroupImport.class))
					.flatMap(this::optionalToStream);
		}

		default <T> Stream<T> optionalToStream(Optional<T> opt) {
			return opt.map(Stream::of).orElse(Stream.empty());
		}

	}

	@Module
	interface ImplicitDeclarations {
		default Stream<Tree<GlobalVarDecl>> get(Tree<?> tree, IRNode node) {
			return Stream.empty();
		}
		default Stream<Tree<GlobalVarDecl>> get(Tree<?> tree, NamespaceDecl ns) {
			return Namespaces.getNamespace(tree.assertNode(ns), ns.getQID())
					.flatMap(nsDecl -> nsDecl.children(NamespaceDecl::getVarDecls))
					.filter(varDecl -> varDecl.node().getAvailability() != Availability.LOCAL);
		}

		default Stream<Tree<GlobalVarDecl>> get(Tree<?> tree, CompilationTask task) {
			return Namespaces.getNamespace(tree.assertNode(task), QID.of("prelude"))
					.flatMap(ns -> ns.children(NamespaceDecl::getVarDecls))
					.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC);
		}

	}

	@Module
	interface Declarations {
		default Stream<Tree<VarDecl>> get(Tree<?> tree, IRNode node) {
			return Stream.empty();
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprLet let) {
			return tree.assertNode(let).children(ExprLet::getVarDecls).map(Tree::upCast);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprLambda lambda) {
			Stream<Tree<ParameterVarDecl>> parameters = tree.assertNode(lambda).children(ExprLambda::getValueParameters);
			return parameters.map(Tree::upCast);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprProc proc) {
			Stream<Tree<ParameterVarDecl>> parameters = tree.assertNode(proc).children(ExprProc::getValueParameters);
			return parameters.map(Tree::upCast);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, ExprComprehension comprehension) {
			return tree.assertNode(comprehension)
					.child(ExprComprehension::getGenerator)
					.children(Generator::getVarDecls)
					.map(Tree::upCast);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, StmtBlock block) {
			return tree.assertNode(block).children(StmtBlock::getVarDecls).map(Tree::upCast);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, StmtForeach foreach) {
			return tree.assertNode(foreach)
					.child(StmtForeach::getGenerator)
					.children(Generator::getVarDecls)
					.map(Tree::upCast);
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, Action action) {
			Tree<Action> actionTree = tree.assertNode(action);
			return Stream.concat(
					actionTree.children(Action::getVarDecls).map(Tree::upCast),
					actionTree.children(Action::getInputPatterns)
							.flatMap(t -> t.children(InputPattern::getVariables)).map(Tree::upCast));
		}
		default Stream<Tree<VarDecl>> get(Tree<?> tree, CalActor actor) {
			Stream<Tree<LocalVarDecl>> varDecls = tree.assertNode(actor).children(CalActor::getVarDecls);
			Stream<Tree<ParameterVarDecl>> parameters = tree.assertNode(actor).children(CalActor::getValueParameters).map(Tree::upCast);
			return Stream.concat(varDecls, parameters).map(Tree::upCast);
		}

		default Stream<Tree<VarDecl>> get(Tree<?> tree, NlNetwork network) {
			Stream<Tree<LocalVarDecl>> varDecls = tree.assertNode(network).children(NlNetwork::getVarDecls);
			Stream<Tree<ParameterVarDecl>> parameters = tree.assertNode(network).children(NlNetwork::getValueParameters).map(Tree::upCast);
			return Stream.concat(varDecls, parameters).map(Tree::upCast);
		}

		default Stream<Tree<VarDecl>> get(Tree<?> tree, ActorMachine actorMachine) {
			Tree<ActorMachine> root = tree.assertNode(actorMachine);
			Stream<Tree<VarDecl>> scopeVars = root.children(ActorMachine::getScopes)
					.flatMap(t -> t.children(Scope::getDeclarations)).map(Tree::upCast);
			Stream<Tree<VarDecl>> parameters = root.children(ActorMachine::getValueParameters).map(Tree::upCast);
			return Stream.concat(scopeVars, parameters);
		}

		default Stream<Tree<VarDecl>> get(Tree<?> tree, NamespaceDecl ns) {
			Tree<NamespaceDecl> nsTree = tree.assertNode(ns);
			Stream<Tree<GlobalVarDecl>> localDecls = nsTree.children(NamespaceDecl::getVarDecls);
			return localDecls.map(Tree::upCast);
		}

	}
}
