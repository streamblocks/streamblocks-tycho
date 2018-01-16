package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityReference;
import se.lth.cs.tycho.ir.module.ModuleDecl;
import se.lth.cs.tycho.ir.module.ModuleExpr;
import se.lth.cs.tycho.phase.TreeShadow;

public interface ModuleDeclarations {

    ModuleKey<ModuleDeclarations> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .bind("moduleScopes").to(task.getModule(ModuleScopes.key))
            .instance();

    ModuleDecl declaration(ModuleExpr entity);

    @Module
    interface Implementation extends ModuleDeclarations {
        @Binding(BindingKind.INJECTED)
        ModuleScopes moduleScopes();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        default ModuleDecl declaration(ModuleExpr moduleExpr) {
            IRNode node = tree().parent(moduleExpr);
            while (node != null) {
                for (ModuleDecl decl : moduleScopes().declarations(node)) {
                    if (decl.getName().equals(moduleExpr.getName())) {
                        return decl;
                    }
                }

                node = tree().parent(node);
            }
            return null;
        }

    }

}
