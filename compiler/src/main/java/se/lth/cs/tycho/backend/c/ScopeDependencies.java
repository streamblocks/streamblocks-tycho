package se.lth.cs.tycho.backend.c;

import se.lth.cs.tycho.analyze.util.AbstractActorMachineTraverser;
import se.lth.cs.tycho.analyze.util.ActorMachineTraverser;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.Variable;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ScopeDependencies {
	private static final ActorMachineTraverser<BitSet> dependencyCollector = new AbstractActorMachineTraverser<BitSet>() {
		@Override
		public void traverseVariable(Variable var, BitSet dependencies) {
			if (var.isScopeVariable()) {
				dependencies.set(var.getScopeId());
			}
			super.traverseVariable(var, dependencies);
		}
	};
	private final Map<Object, BitSet> directDepencencyCache;
	private final Map<Object, BitSet> dependencyCache;
	private final ActorMachine actorMachine;

	public ScopeDependencies(ActorMachine actorMachine) {
		this.actorMachine = actorMachine;
		this.directDepencencyCache = new HashMap<>();
		this.dependencyCache = new HashMap<>();
	}

	private BitSet directDependencies(Transition transition) {
		return collectDirectDependencies(dependencyCollector::traverseTransition, transition);
	}

	private BitSet directDependencies(Condition condition) {
		return collectDirectDependencies(dependencyCollector::traverseCondition, condition);
	}

	private BitSet directDependencies(Scope scope) {
		return collectDirectDependencies(dependencyCollector::traverseScope, scope);
	}

	public BitSet dependencies(Instruction i) {
		if (i instanceof ICall) {
			return dependencies(actorMachine.getTransition(((ICall) i).T()));
		} else if (i instanceof ITest) {
			return dependencies(actorMachine.getCondition(((ITest) i).C()));
		} else {
			return new BitSet();
		}
	}

	public BitSet dependencies(Scope scope) {
		return collectDependencies(this::directDependencies, scope);
	}

	public BitSet dependencies(Transition transition) {
		return collectDependencies(this::directDependencies, transition);
	}

	public BitSet dependencies(Condition condition) {
		return collectDependencies(this::directDependencies, condition);
	}

	private <T> BitSet collectDependencies(Function<T, BitSet> directDependencies, T t) {
		if (dependencyCache.containsKey(t)) {
			return copy(dependencyCache.get(t));
		} else {
			BitSet result = directDependencies.apply(t);
			collectTransitiveClosure(result);
			dependencyCache.put(t, result);
			return copy(result);
		}
	}

	private <T> BitSet collectDirectDependencies(BiConsumer<T, BitSet> collector, T t) {
		if (directDepencencyCache.containsKey(t)) {
			return copy(directDepencencyCache.get(t));
		} else {
			BitSet result = new BitSet();
			collector.accept(t, result);
			directDepencencyCache.put(t, result);
			return copy(result);
		}
	}

	private BitSet collectTransitiveClosure(BitSet deps) {
		BitSet added = new BitSet();
		BitSet dependencies = new BitSet();
		dependencies.or(deps);
		do {
			BitSet oneStep = dependencies.stream()
					.mapToObj(actorMachine.getScopes()::get)
					.map(this::directDependencies)
					.collect(BitSet::new, BitSet::or, BitSet::or);
			added.clear();
			added.or(oneStep);
			added.andNot(dependencies);
			dependencies.or(oneStep);
		} while (!added.isEmpty());
		return dependencies;
	}

	private BitSet copy(BitSet original) {
		BitSet copy = new BitSet();
		copy.or(original);
		return copy;
	}
}
