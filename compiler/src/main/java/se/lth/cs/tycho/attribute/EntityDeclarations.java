package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.entity.nl.EntityReference;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.phase.TreeShadow;

public interface EntityDeclarations {

    ModuleKey<EntityDeclarations> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .bind("entityScopes").to(task.getModule(EntityScopes.key))
            .bind("imports").to(task.getModule(ImportDeclarations.key))
            .bind("globalNames").to(task.getModule(GlobalNames.key))
            .instance();

    GlobalEntityDecl declaration(EntityReference entity);

    @Module
    interface Implementation extends EntityDeclarations {
        @Binding(BindingKind.INJECTED)
        EntityScopes entityScopes();

        @Binding(BindingKind.INJECTED)
        ImportDeclarations imports();

        @Binding(BindingKind.INJECTED)
        GlobalNames globalNames();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        default GlobalEntityDecl declaration(EntityReferenceLocal localRef) {
            IRNode node = tree().parent(localRef);
            while (node != null) {
                for (GlobalEntityDecl decl : entityScopes().declarations(node)) {
                    if (decl.getName().equals(localRef.getName())) {
                        return decl;
                    }
                }

                for (SingleImport imp : imports().entityImports(node)) {
                    if (imp.getLocalName().equals(localRef.getName())) {
                        return globalDeclaration(imp.getGlobalName());
                    }
                }

                for (GroupImport imp : imports().entityGroupImports(node)) {
                    GlobalEntityDecl imported = globalDeclaration(imp.getGlobalName().concat(QID.of(localRef.getName())));
                    if (imported != null) {
                        return imported;
                    }
                }
                node = tree().parent(node);
            }
            return null;
        }

        default GlobalEntityDecl declaration(EntityReferenceGlobal globalRef) {
            return globalDeclaration(globalRef.getGlobalName());
        }

        default GlobalEntityDecl globalDeclaration(QID name) {
            return globalNames().entityDecl(name, false);
        }
    }

}
