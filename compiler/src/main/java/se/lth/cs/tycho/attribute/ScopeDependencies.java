package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface ScopeDependencies {

    ModuleKey<ScopeDependencies> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .bind("freeVariables").to(task.getModule(FreeVariables.key))
            .instance();

    Set<Scope> ofCondition(Condition cond);
    Set<Scope> ofTransition(Transition trans);
    Set<Scope> ofScope(Scope scope);

    Set<Condition> conditionsUsingScope(Scope scope);
    Set<Transition> transitionsUsingScope(Scope scope);
    Set<Scope> scopesUsingScope(Scope scope);

    @Module
    interface Implementation extends ScopeDependencies {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        FreeVariables freeVariables();

        @Binding(BindingKind.LAZY)
        default Map<IRNode, Set<Scope>> deps() {
            return new IdentityHashMap<>();
        }

        @Override
        default Set<Scope> ofCondition(Condition cond) {
            return deps().computeIfAbsent(cond, this::scopeDependencies);
        }

        @Override
        default Set<Scope> ofTransition(Transition trans) {
            return deps().computeIfAbsent(trans, this::scopeDependencies);
        }

        @Override
        default Set<Scope> ofScope(Scope scope) {
            return deps().computeIfAbsent(scope, this::scopeDependencies);
        }

        @Override
        default Set<Condition> conditionsUsingScope(Scope scope) {
            return actorMachine(scope).getConditions().stream()
                    .filter(c -> ofCondition(c).contains(scope))
                    .collect(Collectors.toSet());
        }

        @Override
        default Set<Transition> transitionsUsingScope(Scope scope) {
            return actorMachine(scope).getTransitions().stream()
                    .filter(t -> ofTransition(t).contains(scope))
                    .collect(Collectors.toSet());
        }

        @Override
        default Set<Scope> scopesUsingScope(Scope scope) {
            return actorMachine(scope).getScopes().stream()
                    .filter(s -> ofScope(s).contains(scope))
                    .collect(Collectors.toSet());
        }

        default Set<Scope> scopeDependencies(IRNode node) {
            ActorMachine am = actorMachine(node);
            Set<String> freeVariableNames = freeVariables().freeVariables(node).stream()
                    .map(Variable::getName)
                    .collect(Collectors.toSet());
            return am.getScopes().stream()
                    .filter(scope -> scope.getDeclarations().stream()
                            .anyMatch(var -> freeVariableNames.contains(var.getName())))
                    .collect(Collectors.toSet());
        }

        default ActorMachine actorMachine(IRNode node) {
            return actorMachine(tree().parent(node));
        }

        default ActorMachine actorMachine(ActorMachine am) {
            return am;
        }
    }



}
