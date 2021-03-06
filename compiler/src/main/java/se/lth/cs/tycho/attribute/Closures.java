package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.phase.TreeShadow;

public interface Closures {

    ModuleKey<Closures> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .bind("freeVariables").to(task.getModule(FreeVariables.key))
            .instance();

    /**
     * Returns the nearest parent node that is a closure envirnoment boundary.
     * @param node
     * @return the environment boundary
     */
    IRNode environmentBoundary(IRNode node);

    /**
     * Returns true iff the variable is declared outside its environment boundary.
     * @param var
     * @return true iff the variable is declared outside its environment boundary
     */
    boolean isDeclaredInClosure(Variable var);

    @Module
    interface Implementation extends Closures {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        FreeVariables freeVariables();

        @Override
        default boolean isDeclaredInClosure(Variable var) {
            return freeVariables().freeVariables(environmentBoundary(var)).contains(var);
        }

        @Override
        default IRNode environmentBoundary(IRNode node) {
            return environmentBoundary(tree().parent(node));
        }

        default IRNode environmentBoundary(ExprProc proc) {
            return proc;
        }

        default IRNode environmentBoundary(ExprLambda lambda) {
            return lambda;
        }

        default IRNode environmentBoundary(Entity entity) {
            return entity;
        }
    }

}
