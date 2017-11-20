package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface FreeVariables {
    ModuleKey<FreeVariables> key = (unit, manager) -> MultiJ.from(FreeVariables.Implementation.class)
            .bind("scopes").to(manager.getAttributeModule(VariableScopes.key, unit))
            .instance();

    Set<Variable> freeVariables(IRNode node);

    @Module
    interface Implementation extends FreeVariables {
        @Binding(BindingKind.INJECTED)
        VariableScopes scopes();

        @Override
        default Set<Variable> freeVariables(IRNode node) {
            Set<Variable> free = new LinkedHashSet<>();
            node.forEachChild(child -> free.addAll(freeVariables(child)));
            Set<String> declaredNames = scopes().declarations(node).stream()
                    .map(VarDecl::getName)
                    .collect(Collectors.toSet());
            free.removeIf(var -> declaredNames.contains(var.getName()));
            return free;
        }

        default Set<Variable> freeVariables(Variable var) {
            return Collections.singleton(var);
        }
    }
}
