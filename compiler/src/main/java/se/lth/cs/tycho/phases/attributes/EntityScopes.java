package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.TreeShadow;

import java.util.stream.Stream;

public interface EntityScopes {
    ModuleKey<EntityScopes> key = (unit, manager) -> MultiJ.from(Implementation.class)
            .bind("tree").to(manager.getAttributeModule(TreeShadow.key, unit))
            .instance();

    ImmutableList<GlobalEntityDecl> declarations(IRNode node);

    @Module
    interface Implementation extends EntityScopes {

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Override
        default ImmutableList<GlobalEntityDecl> declarations(IRNode node) {
            return ImmutableList.empty();
        }

        default ImmutableList<GlobalEntityDecl> declarations(NamespaceDecl ns) {
            Stream<GlobalEntityDecl> local = ns.getEntityDecls().stream();

            CompilationTask task = (CompilationTask) tree().root();
            Stream<GlobalEntityDecl> global = task.getSourceUnits().stream()
                    .map(SourceUnit::getTree)
                    .filter(decl -> decl.getQID().equals(ns.getQID()))
                    .map(NamespaceDecl::getEntityDecls)
                    .flatMap(ImmutableList::stream);

            return Stream.concat(local, global)
                    .distinct()
                    .collect(ImmutableList.collector());
        }
    }
}
