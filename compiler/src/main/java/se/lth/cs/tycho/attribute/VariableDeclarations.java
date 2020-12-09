package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.List;
import java.util.Optional;

public interface VariableDeclarations {

    ModuleKey<VariableDeclarations> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .bind("root").to(task)
            .bind("varScopes").to(task.getModule(VariableScopes.key))
            .bind("imports").to(task.getModule(ImportDeclarations.key))
            .bind("globalNames").to(task.getModule(GlobalNames.key))
            .instance();

    VarDecl declaration(Variable var);

    VarDecl declaration(ExprVariable var);

    VarDecl declaration(LValueVariable var);

    VarDecl declaration(ExprGlobalVariable var);

    List<VarDecl> declarations(IRNode var);

    @Module
    interface Implementation extends VariableDeclarations {
        @Binding(BindingKind.INJECTED)
        VariableScopes varScopes();

        @Binding(BindingKind.INJECTED)
        ImportDeclarations imports();

        @Binding(BindingKind.INJECTED)
        GlobalNames globalNames();

        @Binding(BindingKind.INJECTED)
        CompilationTask root();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        default VarDecl declaration(ExprVariable var) {
            return declaration(var.getVariable());
        }

        default VarDecl declaration(LValueVariable var) {
            return declaration(var.getVariable());
        }

        default VarDecl declaration(Variable var) {
            IRNode node = tree().parent(var);
            while (node != null) {
                for (VarDecl decl : varScopes().declarations(node)) {
                    if (decl.getName().equals(var.getName())) {
                        return decl;
                    }
                }

                for (SingleImport imp : imports().variableImports(node)) {
                    if (imp.getLocalName().equals(var.getName())) {
                        return globalDeclaration(imp.getGlobalName());
                    }
                }

                for (GroupImport imp : imports().variableGroupImports(node)) {
                    VarDecl imported = globalDeclaration(imp.getGlobalName().concat(QID.of(var.getName())));
                    if (imported != null) {
                        return imported;
                    }
                }

                // -- FIXME: Temporary hack to support println
                Optional<SourceUnit> s = root().getSourceUnits().stream().filter(sourceUnit -> sourceUnit.getTree().getQID().isPrefixOf(QID.of("prelude"))).findAny();
                if(s.isPresent()){
                    NamespaceDecl ns = s.get().getTree();
                    for(VarDecl decl : ns.getVarDecls()){
                        if (decl.getName().equals(var.getName())) {
                            return decl;
                        }
                    }
                }
                node = tree().parent(node);
            }

            return null;
        }

        default VarDecl declaration(ExprGlobalVariable globalVar) {
            return globalDeclaration(globalVar.getGlobalName());
        }

        default VarDecl globalDeclaration(QID name) {
            return globalNames().varDecl(name, false);
        }

        default List<VarDecl> declarations(IRNode node) {
            ImmutableList.Builder<VarDecl> decls = ImmutableList.builder();
            node.forEachChild(child -> decls.addAll(declarations(child)));
            return decls.build();
        }

        default List<VarDecl> declarations(ExprVariable exprVariable) {
            return ImmutableList.of(declaration(exprVariable));
        }

    }

}
