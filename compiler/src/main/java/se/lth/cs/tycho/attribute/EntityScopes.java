package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.stream.Stream;

public interface EntityScopes {
    ModuleKey<EntityScopes> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
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
