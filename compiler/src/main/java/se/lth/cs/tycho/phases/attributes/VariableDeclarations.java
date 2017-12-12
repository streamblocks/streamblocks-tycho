package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.phases.TreeShadow;

public interface VariableDeclarations {

    ModuleKey<VariableDeclarations> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .bind("varScopes").to(task.getModule(VariableScopes.key))
            .bind("imports").to(task.getModule(ImportDeclarations.key))
            .bind("globalNames").to(task.getModule(GlobalNames.key))
            .instance();

    VarDecl declaration(Variable var);
    VarDecl declaration(ExprVariable var);
    VarDecl declaration(LValueVariable var);
    VarDecl declaration(ExprGlobalVariable var);

    @Module
    interface Implementation extends VariableDeclarations {
        @Binding(BindingKind.INJECTED)
        VariableScopes varScopes();

        @Binding(BindingKind.INJECTED)
        ImportDeclarations imports();

        @Binding(BindingKind.INJECTED)
        GlobalNames globalNames();

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
    }

}
