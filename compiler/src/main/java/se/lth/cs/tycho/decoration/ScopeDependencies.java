package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.util.Optionals;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScopeDependencies {
	private ScopeDependencies() {}

	public static Set<Tree<Scope>> conditionDependencies(Tree<Condition> condition) {
		return dependencies(condition);
	}

	public static Set<Tree<Scope>> transitionDependencies(Tree<Transition> transition) {
		return dependencies(transition);
	}

	public static Set<Tree<Scope>> scopeDependencies(Tree<Scope> scope) {
		return dependencies(scope);
	}

	private static Set<Tree<Scope>> scopeReferences(Tree<?> node) {
		return node.walk()
				.map(declIfVar)
				.flatMap(Optionals::toStream)
				.map(decl -> decl.findParentOfType(Scope.class))
				.flatMap(Optionals::toStream)
				.collect(Collectors.toSet());
	}

	private static Set<Tree<Scope>> dependencies(Tree<?> node) {
		Queue<Tree<?>> queue = new ArrayDeque<>();
		Set<Tree<Scope>> result = new HashSet<>(scopeReferences(node));
		queue.addAll(result);
		while (!queue.isEmpty()) {
			Tree<?> n = queue.remove();
			for (Tree<Scope> scope : scopeReferences(n)) {
				if (result.add(scope)) {
					queue.add(scope);
				}
			}
		}
		return result;
	}

	private static final DeclIfVar declIfVar = MultiJ.instance(DeclIfVar.class);

	@Module
	interface DeclIfVar extends Function<Tree<?>, Optional<Tree<? extends VarDecl>>> {
		@Override
		default Optional<Tree<? extends VarDecl>> apply(Tree<?> node) {
			return declIfVar(node.node(), node);
		}

		default Optional<Tree<? extends VarDecl>> declIfVar(IRNode node, Tree<?> tree) {
			return Optional.empty();
		}

		default Optional<Tree<? extends VarDecl>> declIfVar(Variable var, Tree<?> tree) {
			return VariableDeclarations.getDeclaration(tree.assertNode(var));
		}

	}
}
