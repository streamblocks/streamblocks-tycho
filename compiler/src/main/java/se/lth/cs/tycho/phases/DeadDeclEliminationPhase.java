package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.decoration.EntityDeclarations;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.decoration.VariableDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.util.Optionals;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

public class DeadDeclEliminationPhase implements Phase {
	@Override
	public String getDescription() {
		return "Removes unused declarations.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Set<Tree<Decl>> usedDeclarations = usedDeclarations(task);
		Liveness liveness = usedDeclarations::contains;
		Filter filter = MultiJ.from(Filter.class)
				.bind("liveness").to(liveness)
				.instance();
		IRNode result = filter.apply(Tree.of(task));
		return (CompilationTask) result;
	}

	private Set<Tree<Decl>> usedDeclarations(CompilationTask task) {
		Declarations declarations = MultiJ.instance(Declarations.class);
		Set<Tree<Decl>> decls = new HashSet<>();
		Queue<Tree<Decl>> queue = new ArrayDeque<>();
		Tree.of(task).child(CompilationTask::getNetwork).walk()
				.map(declarations)
				.flatMap(Optionals::toStream)
				.peek(decls::add)
				.forEach(queue::add);
		while (!queue.isEmpty()) {
			Tree<Decl> decl = queue.remove();
			decl.walk()
					.map(declarations)
					.flatMap(Optionals::toStream)
					.filter(d -> !decls.contains(d))
					.peek(decls::add)
					.forEach(queue::add);
		}
		return decls;
	}

	@FunctionalInterface
	interface Liveness {
		boolean isAlive(Tree<?> node);
	}

	@Module
	interface Declarations extends Function<Tree<?>, Optional<Tree<Decl>>> {
		@Override
		default Optional<Tree<Decl>> apply(Tree<?> node) {
			return getDeclaration(node, node.node());
		}

		default Optional<Tree<Decl>> getDeclaration(Tree<?> tree, IRNode node) {
			return Optional.empty();
		}

		default Optional<Tree<Decl>> getDeclaration(Tree<?> tree, Variable var) {
			return VariableDeclarations.getDeclaration(tree.assertNode(var)).map(Tree::upCast);
		}

		default Optional<Tree<Decl>> getDeclaration(Tree<?> tree, ExprGlobalVariable var) {
			return VariableDeclarations.getGlobalVariableDeclaration(tree.assertNode(var)).map(Tree::upCast);
		}

		default Optional<Tree<Decl>> getDeclaration(Tree<?> tree, Instance instance) {
			return EntityDeclarations.getInstanceDeclaration(tree.assertNode(instance)).map(Tree::upCast);
		}
	}

	@Module
	interface Filter extends Function<Tree<?>, IRNode> {
		@Binding(BindingKind.INJECTED)
		Liveness liveness();

		@Override
		default IRNode apply(Tree<?> tree) {
			return transform(tree, tree.node());
		}

		default IRNode transform(Tree<?> tree, IRNode node) {
			return tree.assertNode(node).transformChildren(this);
		}

		default NamespaceDecl transform(Tree<?> tree, NamespaceDecl ns) {
			Tree<NamespaceDecl> nsTree = tree.assertNode(ns);
			List<GlobalEntityDecl> entities = filter(nsTree, NamespaceDecl::getEntityDecls, GlobalEntityDecl.class);
			List<GlobalVarDecl> variables = filter(nsTree, NamespaceDecl::getVarDecls, GlobalVarDecl.class);
			List<GlobalTypeDecl> types = filter(nsTree, NamespaceDecl::getTypeDecls, GlobalTypeDecl.class);
			return ns.withEntityDecls(entities)
					.withVarDecls(variables)
					.withTypeDecls(types);
		}

		default CalActor transform(Tree<?> tree, CalActor actor) {
			Tree<CalActor> actorTree = tree.assertNode(actor);
			List<LocalVarDecl> variables = filter(actorTree, CalActor::getVarDecls, LocalVarDecl.class);
			return actor.withVarDecls(variables);
		}

		default <P extends IRNode, C extends IRNode> List<C> filter(Tree<P> parent, Function<P, Collection<C>> getChildren, Class<C> childType) {
			ImmutableList<Tree<C>> children = parent.children(getChildren).collect(ImmutableList.collector());
			ImmutableList<C> result = children.stream()
					.filter(liveness()::isAlive)
					.map(node -> childType.cast(apply(node)))
					.collect(ImmutableList.collector());
			return result;
		}
	}
}
