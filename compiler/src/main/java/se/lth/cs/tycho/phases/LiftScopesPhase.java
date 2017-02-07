package se.lth.cs.tycho.phases;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.decoration.ScopeDependencies;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LiftScopesPhase implements Phase {
	public final OnOffSetting liftScopes = new OnOffSetting() {
		@Override public String getKey() { return "lift-scopes"; }
		@Override public String getDescription() { return "Lift actor machine scopes to conditions or transitions when only used from one place."; }
		@Override public Boolean defaultValue(Configuration configuration) { return true; }
	};

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return ImmutableList.of(liftScopes);
	}

	@Override
	public String getDescription() {
		return "Lifts actor machien scopes to conditions or transitions when only used from one place.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		if (context.getConfiguration().get(liftScopes)) {
			return (CompilationTask) Tree.of(task).transformChildren(lifter);
		}
		return task;
	}

	private static Lifter lifter = MultiJ.instance(Lifter.class);

	@Module
	interface Lifter extends Function<Tree<?>, IRNode> {
		@Override
		default IRNode apply(Tree<?> tree) {
			return transform(tree.node(), tree);
		}

		default IRNode transform(IRNode node, Tree<?> tree) {
			return tree.transformChildren(this);
		}

		default IRNode transform(ActorMachine a, Tree<?> tree) {
			Tree<ActorMachine> actor = tree.assertNode(a);
			Dependencies dependencies = new Dependencies();
			dependencies.collect(actor);
			List<Condition> conditions = actor.children(ActorMachine::getConditions)
					.map(dependencies::transformCondition)
					.collect(Collectors.toList());
			List<Transition> transitions = actor.children(ActorMachine::getTransitions)
					.map(dependencies::transformTransition)
					.collect(Collectors.toList());
			List<Scope> scopes = actor.children(ActorMachine::getScopes)
					.map(dependencies::transformScope)
					.collect(Collectors.toList());
			return actor.node()
					.withConditions(conditions)
					.withTransitions(transitions)
					.withScopes(scopes);
		}
	}

	private static class Dependencies {
		private final Map<Tree<?>, Set<Tree<Scope>>> dependencies;
		private final Map<Tree<Scope>, Set<Tree<?>>> reverseDependencies;

		public Dependencies() {
			dependencies = new HashMap<>();
			reverseDependencies = new HashMap<>();
		}

		public Condition transformCondition(Tree<Condition> condition) {
			if (condition.node() instanceof PredicateCondition) {
				Set<Tree<Scope>> liftable = getLiftableScopes(condition);
				PredicateCondition cond = (PredicateCondition) condition.node();
				if (!liftable.isEmpty()) {
					assert liftable.size() == 1; // TODO implement lifting of several scopes
					Scope s = liftable.iterator().next().node();
					Expression expr = cond.getExpression();
					expr = new ExprLet(ImmutableList.empty(), s.getDeclarations(), expr);
					return new PredicateCondition(expr);
				}
			}
			return condition.node();
		}

		public Transition transformTransition(Tree<Transition> transition) {
			Set<Tree<Scope>> liftable = getLiftableScopes(transition);
			if (!liftable.isEmpty()) {
				assert liftable.size() == 1;
				Scope s = liftable.iterator().next().node();
				ImmutableList<Statement> statements = transition.node().getBody();
				Statement statement = new StmtBlock(ImmutableList.empty(), s.getDeclarations(), statements);
				return transition.node().withBody(ImmutableList.of(statement));
			} else {
				return transition.node();
			}
		}

		public Scope transformScope(Tree<Scope> scope) {
			if (isLiftableScope(scope)) {
				return new Scope(ImmutableList.empty(), false);
			} else {
				return scope.node();
			}
		}

		private Set<Tree<Scope>> getLiftableScopes(Tree<?> node) {
			return dependencies(node).stream()
					.filter(this::isLiftableScope)
					.collect(Collectors.toSet());
		}

		private boolean isLiftableScope(Tree<Scope> scope) {
			if (scope.node().isPersistent()) return false;
			Set<Tree<?>> revDeps = reverseDependencies(scope);
			return revDeps.size() == 1;
		}

		private Set<Tree<Scope>> dependencies(Tree<?> revDep) {
			return dependencies.getOrDefault(revDep, Collections.emptySet());
		}

		private Set<Tree<?>> reverseDependencies(Tree<Scope> scope) {
			return reverseDependencies.getOrDefault(scope, Collections.emptySet());
		}

		public void collect(Tree<ActorMachine> actorMachine) {
			actorMachine.children(ActorMachine::getConditions).forEach(c -> collect(c, ScopeDependencies::conditionDependencies));
			actorMachine.children(ActorMachine::getTransitions).forEach(c -> collect(c, ScopeDependencies::transitionDependencies));
			actorMachine.children(ActorMachine::getScopes).forEach(c -> collect(c, ScopeDependencies::scopeDependencies));
		}

		public <T extends IRNode> void collect(Tree<T> node, Function<Tree<T>, Set<Tree<Scope>>> computeDeps) {
			Set<Tree<Scope>> deps = computeDeps.apply(node);
			addAll(dependencies, node, deps);
			addToAll(reverseDependencies, deps, node);
		}

		private <K, V> void addToAll(Map<K, Set<V>> map, Set<K> keys, V value) {
			for (K key : keys) {
				map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
			}
		}

		private <K, V> void addAll(Map<K,Set<V>> map, K key, Set<V> values) {
			map.computeIfAbsent(key, k -> new HashSet<>()).addAll(values);
		}
	}

}
