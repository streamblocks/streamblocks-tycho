package se.lth.cs.tycho.attribute;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.stream.Stream;

public interface ImportDeclarations {
    ModuleKey<ImportDeclarations> key = task -> MultiJ.instance(Implementation.class);

    ImmutableList<SingleImport> variableImports(IRNode node);
    ImmutableList<SingleImport> entityImports(IRNode node);
    ImmutableList<SingleImport> typeImports(IRNode node);

    ImmutableList<GroupImport> variableGroupImports(IRNode node);
    ImmutableList<GroupImport> entityGroupImports(IRNode node);
    ImmutableList<GroupImport> typeGroupImports(IRNode node);


    @Module
    interface Implementation extends ImportDeclarations {

        @Override
        default ImmutableList<SingleImport> variableImports(IRNode node) {
            return imports(node, Import.Kind.VAR);
        }

        @Override
        default ImmutableList<SingleImport> entityImports(IRNode node) {
            return imports(node, Import.Kind.ENTITY);
        }

        @Override
        default ImmutableList<SingleImport> typeImports(IRNode node) {
            return imports(node, Import.Kind.TYPE);
        }

        @Override
        default ImmutableList<GroupImport> variableGroupImports(IRNode node) {
            return groupImports(node, Import.Kind.VAR);
        }

        @Override
        default ImmutableList<GroupImport> entityGroupImports(IRNode node) {
            return groupImports(node, Import.Kind.ENTITY);
        }

        @Override
        default ImmutableList<GroupImport> typeGroupImports(IRNode node) {
            return groupImports(node, Import.Kind.TYPE);
        }

        default ImmutableList<SingleImport> imports(IRNode node, Import.Kind kind) {
            return ImmutableList.empty();
        }

        default ImmutableList<SingleImport> imports(NamespaceDecl ns, Import.Kind kind) {
            return ns.getImports().stream()
                    .filter(imp -> imp.getKind() == kind)
                    .flatMap(this::ifSingleImport)
                    .collect(ImmutableList.collector());
        }


        default ImmutableList<GroupImport> groupImports(IRNode node, Import.Kind kind) {
            return ImmutableList.empty();
        }

        default ImmutableList<GroupImport> groupImports(NamespaceDecl ns, Import.Kind kind) {
            return ns.getImports().stream()
                    .filter(imp -> imp.getKind() == kind)
                    .flatMap(this::ifGroupImport)
                    .collect(ImmutableList.collector());
        }

        default Stream<SingleImport> ifSingleImport(Import imp) {
            return Stream.empty();
        }

        default Stream<SingleImport> ifSingleImport(SingleImport imp) {
            return Stream.of(imp);
        }

        default Stream<GroupImport> ifGroupImport(Import imp) {
            return Stream.empty();
        }

        default Stream<GroupImport> ifGroupImport(GroupImport imp) {
            return Stream.of(imp);
        }
    }
}
