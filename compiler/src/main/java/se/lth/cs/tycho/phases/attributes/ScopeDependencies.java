package se.lth.cs.tycho.phases.attributes;

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
import se.lth.cs.tycho.phases.TreeShadow;

import java.util.Set;
import java.util.stream.Collectors;

public interface ScopeDependencies {

    ModuleKey<ScopeDependencies> key = (task, manager) -> MultiJ.from(Implementation.class)
            .bind("tree").to(manager.getAttributeModule(TreeShadow.key, task))
            .bind("freeVariables").to(manager.getAttributeModule(FreeVariables.key, task))
            .instance();

    Set<Scope> ofCondition(Condition cond);
    Set<Scope> ofTransition(Transition trans);
    Set<Scope> ofScope(Scope scope);

    @Module
    interface Implementation extends ScopeDependencies {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding
        FreeVariables freeVariables();

        @Override
        default Set<Scope> ofCondition(Condition cond) {
            return scopeDependencies(cond);
        }

        @Override
        default Set<Scope> ofTransition(Transition trans) {
            return scopeDependencies(trans);
        }

        @Override
        default Set<Scope> ofScope(Scope scope) {
            return scopeDependencies(scope);
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
