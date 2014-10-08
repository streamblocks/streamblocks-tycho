package se.lth.cs.tycho.backend.c.att;

import java.util.HashSet;
import java.util.Set;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import javarag.Bottom;
import javarag.Circular;
import javarag.Collected;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.CollectionBuilder;
import javarag.coll.Collector;

public class ScopeDependencies extends Module<ScopeDependencies.Decls> {

	public interface Decls {

		@Inherited
		IRNode lookupEnclosingScopeOrTransitionOrCondition(Variable node, Variable var);

		@Synthesized
		IRNode enclosingScopeOrTransitionOrCondition(Variable v);

		@Collected
		Set<Scope> directScopeDependencies(Condition t);

		@Collected
		Set<Scope> directScopeDependencies(Transition t);

		@Collected
		Set<Scope> directScopeDependencies(Scope s);

		@Circular
		@Synthesized
		Set<Scope> scopeDependencies(Scope dep);

		@Synthesized
		Set<Scope> requiredScopes(Transition t);

		@Synthesized
		Set<Scope> requiredScopes(Condition c);

		ActorMachine actorMachine(IRNode n);
	}

	public Scope lookupEnclosingScopeOrTransitionOrCondition(ActorMachine am, Variable v) {
		return null;
	}

	public Scope lookupEnclosingScopeOrTransitionOrCondition(Scope s, Variable v) {
		return s;
	}

	public Transition lookupEnclosingScopeOrTransitionOrCondition(Transition t, Variable v) {
		return t;
	}

	public PredicateCondition lookupEnclosingScopeOrTransitionOrCondition(PredicateCondition c, Variable v) {
		return c;
	}

	public IRNode enclosingScopeOrTransitionOrCondition(Variable var) {
		return e().lookupEnclosingScopeOrTransitionOrCondition(var, var);
	}

	public Builder<Set<Scope>, Scope> directScopeDependencies(Transition t) {
		return new CollectionBuilder<Set<Scope>, Scope>(new HashSet<Scope>());
	}

	public Builder<Set<Scope>, Scope> directScopeDependencies(Scope s) {
		return new CollectionBuilder<Set<Scope>, Scope>(new HashSet<Scope>());
	}

	public Builder<Set<Scope>, Scope> directScopeDependencies(Condition c) {
		return new CollectionBuilder<Set<Scope>, Scope>(new HashSet<Scope>());
	}

	public void directScopeDependencies(Variable v, Collector<Scope> coll) {
		if (v.isScopeVariable()) {
			IRNode encl = e().enclosingScopeOrTransitionOrCondition(v);
			if (encl != null) {
				Scope s = e().actorMachine(v).getScopes().get(v.getScopeId());
				coll.add(encl, s);
			}
		}
	}

	@Bottom("scopeDependencies")
	public Set<Scope> scopeDependenciesStart(Scope s) {
		return new HashSet<>();
	}

	public Set<Scope> scopeDependencies(Scope s) {
		Set<Scope> result = new HashSet<>();
		Set<Scope> direct = e().directScopeDependencies(s);
		result.addAll(direct);
		for (Scope dep : direct) {
			result.addAll(e().scopeDependencies(dep));
		}
		return result;
	}

	public Set<Scope> requiredScopes(Transition t) {
		Set<Scope> result = new HashSet<>();
		for (Scope s : e().directScopeDependencies(t)) {
			result.add(s);
			result.addAll(e().scopeDependencies(s));
		}
		return result;
	}

	public Set<Scope> requiredScopes(Condition c) {
		Set<Scope> result = new HashSet<>();
		for (Scope s : e().directScopeDependencies(c)) {
			result.add(s);
			result.addAll(e().scopeDependencies(s));
		}
		return result;
	}
}
