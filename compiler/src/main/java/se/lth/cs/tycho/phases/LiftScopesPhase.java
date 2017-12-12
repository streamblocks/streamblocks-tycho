package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.attributes.ScopeDependencies;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.List;
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
			Transformation t = MultiJ.from(Transformation.class)
					.bind("scopes").to(task.getModule(ScopeDependencies.key))
                    .instance();
		    return task.transformChildren(t);
		}
		return task;
	}

	@Module
	interface Transformation extends IRNode.Transformation {
		@Binding(BindingKind.INJECTED)
		se.lth.cs.tycho.phases.attributes.ScopeDependencies scopes();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default boolean canBeLifted(Scope s) {
			return !s.isPersistent() && scopes().transitionsUsingScope(s).size() +
                    scopes().conditionsUsingScope(s).size() +
                    scopes().scopesUsingScope(s).size() == 1;
		}

		default Transition apply(Transition trans) {
		    List<LocalVarDecl> variables = scopes().ofTransition(trans).stream()
                    .filter(this::canBeLifted)
                    .flatMap(s -> s.getDeclarations().stream())
					.collect(Collectors.toList());
		    if (variables.isEmpty()) {
		    	return trans;
			} else {
		    	return trans.withBody(Collections.singletonList(new StmtBlock(Collections.emptyList(), variables, trans.getBody())));
			}
		}

		default PredicateCondition apply(PredicateCondition cond) {
			List<LocalVarDecl> variables = scopes().ofCondition(cond).stream()
					.filter(this::canBeLifted)
					.flatMap(s -> s.getDeclarations().stream())
					.collect(Collectors.toList());
			if (variables.isEmpty()) {
				return cond;
			} else {
				return cond.copy(new ExprLet(Collections.emptyList(), variables, cond.getExpression()));
			}
		}

		default Scope apply(Scope s) {
			if (canBeLifted(s)) {
				return s.copy(ImmutableList.empty(), s.isPersistent());
			} else {
				return s;
			}
		}

		default Condition apply(Condition cond) {
			return cond;
		}
	}
}
