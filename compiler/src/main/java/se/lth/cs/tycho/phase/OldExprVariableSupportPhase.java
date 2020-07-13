package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.ArrayList;
import java.util.List;

public class OldExprVariableSupportPhase implements Phase {
    @Override
    public String getDescription() {
        return "Support for old variable reference.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Transformation transformation = MultiJ.from(OldExprVariableSupportPhase.Transformation.class)
                .bind("declarations").to(task.getModule(VariableDeclarations.key))
                .bind("tree").to(task.getModule(TreeShadow.key))
                .bind("reporter").to(context.getReporter())
                .instance();
        return task.transformChildren(transformation);
    }

    @Module
    interface Transformation extends IRNode.Transformation {
        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        @Binding(BindingKind.INJECTED)
        VariableDeclarations declarations();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        Reporter reporter();

        @Binding(BindingKind.LAZY)
        default List oldVarDecls() {
            return new ArrayList<>();
        }

        default IRNode apply(Action action) {
            // -- Visit all children
            Action transAction = action.transformChildren(this);

            // -- No old keyword used in this action
            if (oldVarDecls().isEmpty()) {
                return action;
            }

            // -- Copy all the actions local variables plus all the new "old" variable
            // declarations
            ImmutableList.Builder<LocalVarDecl> varDecls = ImmutableList.builder();
            varDecls.addAll(action.getVarDecls());
            varDecls.addAll(oldVarDecls());

            // -- Clear variables
            oldVarDecls().clear();

            // -- Returned new modified action
            return transAction.withVarDecls(varDecls.build());
        }

        default IRNode apply(ExprVariable variable) {
            // -- Bypass all ExprVariable that does not have the old
            if (!variable.getOld()) {
                return variable;
            }

            // -- Make sure that the old keyword is used only in the body of an action
            IRNode parent = tree().parent(variable);
            while (parent != null && !(parent instanceof Action)) {
                parent = tree().parent(parent);
            }

            if (parent == null) {
                reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "The \"old\" keyword can be used only inside actions.", sourceUnit(variable), variable));
            }

            VarDecl decl = declarations().declaration(variable);

            // -- Create a single instance of a old reference variable
            String name = "$old_" + decl.getName();
            VarDecl oldDecl = new LocalVarDecl(decl.getAnnotations(), (TypeExpr) decl.getType().clone(), name, new ExprVariable(Variable.variable(decl.getName())), true);
            if (!containsOldVarDecl(oldDecl)) {
                oldVarDecls().add(oldDecl);
            }

            // -- Return the new ExprVar
            return new ExprVariable(Variable.variable(name));
        }

        default SourceUnit sourceUnit(IRNode node) {
            return sourceUnit(tree().parent(node));
        }

        default SourceUnit sourceUnit(SourceUnit unit) {
            return unit;
        }

        default boolean containsOldVarDecl(VarDecl decl) {
            List<VarDecl> list = oldVarDecls();
            for (VarDecl v : list) {
                if (decl.getName().equals(v.getName())) {
                    return true;
                }
            }

            return false;
        }

    }

}
