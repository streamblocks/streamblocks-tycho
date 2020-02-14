package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.stream.Stream;

public interface TypeDeclarations {
    ModuleKey<TypeDeclarations> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .instance();

    ImmutableList<TypeDecl> declarations(IRNode node);

    @Module
    interface Implementation extends TypeDeclarations {

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Override
        default ImmutableList<TypeDecl> declarations(IRNode node) {
            return ImmutableList.empty();
        }

        default ImmutableList<TypeDecl> declarations(NamespaceDecl ns) {
            Stream<GlobalTypeDecl> local = ns.getTypeDecls().stream();

            CompilationTask task = (CompilationTask) tree().root();
            Stream<GlobalTypeDecl> global = task.getSourceUnits().stream()
                    .map(SourceUnit::getTree)
                    .filter(decl -> decl.getQID().equals(ns.getQID()))
                    .map(NamespaceDecl::getTypeDecls)
                    .flatMap(ImmutableList::stream);

            return Stream.concat(local, global)
                    .distinct()
                    .collect(ImmutableList.collector());
        }
    }
}
