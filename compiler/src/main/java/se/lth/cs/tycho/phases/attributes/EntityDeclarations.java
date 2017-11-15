package se.lth.cs.tycho.phases.attributes;

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
import se.lth.cs.tycho.phases.TreeShadow;

public interface EntityDeclarations {

    ModuleKey<EntityDeclarations> key = (unit, manager) -> MultiJ.from(Implementation.class)
            .bind("tree").to(manager.getAttributeModule(TreeShadow.key, unit))
            .bind("entityScopes").to(manager.getAttributeModule(EntityScopes.key, unit))
            .bind("imports").to(manager.getAttributeModule(ImportDeclarations.key, unit))
            .bind("globalNames").to(manager.getAttributeModule(GlobalNames.key, unit))
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
