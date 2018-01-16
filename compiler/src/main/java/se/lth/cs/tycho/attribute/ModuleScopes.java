package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.module.ModuleDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.stream.Stream;

public interface ModuleScopes {
    ModuleKey<ModuleScopes> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .instance();

    ImmutableList<ModuleDecl> declarations(IRNode node);

    @Module
    interface Implementation extends ModuleScopes {

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Override
        default ImmutableList<ModuleDecl> declarations(IRNode node) {
            return ImmutableList.empty();
        }

        default ImmutableList<ModuleDecl> declarations(NamespaceDecl ns) {
            Stream<ModuleDecl> local = ns.getModuleDecls().stream();

            CompilationTask task = (CompilationTask) tree().root();
            Stream<ModuleDecl> global = task.getSourceUnits().stream()
                    .map(SourceUnit::getTree)
                    .filter(decl -> decl.getQID().equals(ns.getQID()))
                    .map(NamespaceDecl::getModuleDecls)
                    .flatMap(ImmutableList::stream);

            return Stream.concat(local, global)
                    .distinct()
                    .collect(ImmutableList.collector());
        }
    }
}
