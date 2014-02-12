package net.opendf.backend.c.att;

import java.util.HashSet;
import java.util.Set;

import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.Variable;
import javarag.CollectionBuilder;
import javarag.CollectionContribution;
import javarag.FixedPointStart;
import javarag.FixedPointStep;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.CollectionWrapper;

public class ScopeDependencies extends Module<ScopeDependencies.Required> {
	
	@Inherited
	public Scope lookupEnclosingScopeOrTransitionOrCondition(ActorMachine am, Variable v) {
		return null;
	}

	@Inherited
	public Scope lookupEnclosingScopeOrTransitionOrCondition(Scope s, Variable v) {
		return s;
	}

	@Inherited
	public Transition lookupEnclosingScopeOrTransitionOrCondition(Transition t, Variable v) {
		return t;
	}
	
	@Inherited
	public PredicateCondition lookupEnclosingScopeOrTransitionOrCondition(PredicateCondition c, Variable v) {
		return c;
	}

	@Synthesized
	public IRNode enclosingScopeOrTransitionOrCondition(Variable var) {
		return get().lookupEnclosingScopeOrTransitionOrCondition(var, var);
	}

	@CollectionBuilder("directScopeDependencies")
	public Builder directScopeDependenciesBuilder(Transition t) {
		return new CollectionWrapper(new HashSet<>());
	}

	@CollectionBuilder("directScopeDependencies")
	public Builder directScopeDependenciesBuilder(Scope s) {
		return new CollectionWrapper(new HashSet<>());
	}

	@CollectionBuilder("directScopeDependencies")
	public Builder directScopeDependenciesBuilder(Condition c) {
		return new CollectionWrapper(new HashSet<>());
	}

	@CollectionContribution
	public void directScopeDependencies(Variable v) {
		if (v.isScopeVariable()) {
			IRNode encl = get().enclosingScopeOrTransitionOrCondition(v);
			if (encl != null) {
				Scope s = get().actorMachine(v).getScopes().get(v.getScopeId());
				contribute(encl, s);
			}
		}
	}
	
	@FixedPointStart("scopeDependencies")
	@Synthesized
	public Set<Scope> scopeDependenciesStart(Scope s) {
		return new HashSet<>();
	}
	
	@FixedPointStep
	@Synthesized
	public Set<Scope> scopeDependencies(Scope s) {
		Set<Scope> result = new HashSet<>();
		Set<Scope> direct = get().directScopeDependencies(s);
		result.addAll(direct);
		for (Scope dep : direct) {
			result.addAll(get().scopeDependencies(dep));
		}
		return result;
	}
	
	@Synthesized
	public Set<Scope> requiredScopes(Transition t) {
		Set<Scope> result = new HashSet<>();
		for (Scope s : get().directScopeDependencies(t)) {
			result.add(s);
			result.addAll(get().scopeDependencies(s));
		}
		return result;
	}

	@Synthesized
	public Set<Scope> requiredScopes(Condition c) {
		Set<Scope> result = new HashSet<>();
		for (Scope s : get().directScopeDependencies(c)) {
			result.add(s);
			result.addAll(get().scopeDependencies(s));
		}
		return result;
	}

	public interface Required {

		IRNode lookupEnclosingScopeOrTransitionOrCondition(Variable node, Variable var);

		Set<Scope> directScopeDependencies(Condition t);

		Set<Scope> directScopeDependencies(Transition t);

		Set<Scope> scopeDependencies(Scope dep);

		ActorMachine actorMachine(IRNode n);

		Set<Scope> directScopeDependencies(Scope s);

		IRNode enclosingScopeOrTransitionOrCondition(Variable v);

	}

}
