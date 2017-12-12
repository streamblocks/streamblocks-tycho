package se.lth.cs.tycho.phases.transformations;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.phases.TreeShadow;
import se.lth.cs.tycho.phases.attributes.VariableDeclarations;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

/**
 * Renames variables.
 */
public final class RenameVariables {

    private RenameVariables() {}

    /**
     * Renames the variables in the {@code tree} using the name generator {@code newName}.
     *
     * The {@code tree} must be a subtree of {@code task}.
     *
     * @param tree the tree to rename
     * @param newName the name generator
     * @param task the compilation task
     * @return a transformed tree with renamed variables
     */
    public static IRNode rename(IRNode tree, Function<VarDecl, String> newName, CompilationTask task) {
        RenameFunction renameFunction = newName::apply;
        return MultiJ.from(Transformation.class)
                .bind("newName").to(renameFunction)
                .bind("variables").to(task.getModule(VariableDeclarations.key))
                .bind("tree").to(task.getModule(TreeShadow.key))
                .instance()
                .apply(tree);
    }

    /**
     * Appends an underscore character and a number generated from {@code numbers} to the original name
     * ({@link VarDecl#getOriginalName()}) of the variable.
     *
     * @param variablesToRename variables to rename
     * @param numbers number generator
     * @param task the root of the transformation
     * @return a transformed tree with renamed variables.
     */
    public static IRNode appendNumber(IRNode tree, Predicate<VarDecl> variablesToRename, LongSupplier numbers, CompilationTask task) {
        Map<VarDecl, String> renameTable = new HashMap<>();
        Function<VarDecl, String> createName = var -> var.getOriginalName() + "_" + numbers.getAsLong();
        return rename(tree, var -> variablesToRename.test(var) ? renameTable.computeIfAbsent(var, createName) : var.getName(), task);
    }

    @FunctionalInterface
    interface RenameFunction {
        String apply(VarDecl varDecl);
    }

    @Module
    interface Transformation extends IRNode.Transformation {
        @Binding(BindingKind.INJECTED)
        RenameFunction newName();

        @Binding(BindingKind.INJECTED)
        VariableDeclarations variables();

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Override
        default IRNode apply(IRNode root) {
            return root.transformChildren(this);
        }

        default CompilationTask apply(CompilationTask task) {
            return task.transformChildren(this);
        }

        default VarDecl apply(VarDecl decl) {
            String name = newName().apply(decl);
            return decl.transformChildren(this).withName(name);
        }

        default Variable apply(Variable var) {
            VarDecl declaration = variables().declaration(var);
            assert declaration != null;
            String name = newName().apply(declaration);
            return var.withName(name);
        }

        default boolean isEntity(IRNode node) {
            return false;
        }

        default boolean isEntity(Entity node) {
            return true;
        }
    }
}
