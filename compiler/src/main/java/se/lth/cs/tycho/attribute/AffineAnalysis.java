package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.StmtForeach;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Affine Analysis for StmtForeach statements.
 * 
 * An affine loop is a loop where:
 * 1. ITERATION SCOPE:
 *    - The loop collection is an affine structure (e.g., lists, comprehensions with affine bounds)
 *    - The loop filters are affine expressions (linear combinations of variables and constants)
 * 2. LOOP BODY:
 *    - Array/indexer accesses use affine index expressions
 *    - Assignments to loop-carried variables are affine
 *    - No modifications to variables used in loop bounds/filters
 *    - No non-affine control flow (breaks based on non-affine conditions, etc.)
 * 
 * Note: This analysis can be configured to check only iteration scope (lightweight)
 * or both iteration scope and body (comprehensive).
 */
public interface AffineAnalysis {
    ModuleKey<AffineAnalysis> key = task -> MultiJ.from(Implementation.class)
            .bind("types").to(task.getModule(Types.key))
            .bind("freeVariables").to(task.getModule(FreeVariables.key))
            .instance();

    /**
     * Checks if a StmtForeach is affine (iteration scope only).
     * This is a lightweight check that only examines the collection and filters,
     * not the loop body.
     * 
     * @param foreach the foreach statement to check
     * @return true if the foreach iteration scope is affine, false otherwise
     */
    boolean isAffineIterationScope(StmtForeach foreach);

    /**
     * Checks if a StmtForeach is affine (comprehensive check).
     * This checks both the iteration scope AND the loop body for affine properties.
     * 
     * @param foreach the foreach statement to check
     * @return true if the foreach statement is fully affine, false otherwise
     */
    boolean isAffine(StmtForeach foreach);

    /**
     * Checks if the body of a foreach loop is affine.
     * This verifies that array accesses, assignments, and control flow are affine.
     * 
     * @param foreach the foreach statement whose body to check
     * @return true if the loop body is affine, false otherwise
     */
    boolean isAffineBody(StmtForeach foreach);

    /**
     * Checks if a list comprehension is affine (iteration scope only).
     * This checks if the generator and filters are affine.
     * 
     * @param comprehension the list comprehension to check
     * @return true if the comprehension iteration scope is affine, false otherwise
     */
    boolean isAffineComprehensionScope(ExprComprehension comprehension);

    /**
     * Checks if a list comprehension is fully affine.
     * This checks the generator, filters, AND the collection expression being generated.
     * 
     * @param comprehension the list comprehension to check
     * @return true if the comprehension is fully affine, false otherwise
     */
    boolean isAffineComprehension(ExprComprehension comprehension);

    /**
     * Checks if an expression is affine.
     * @param expr the expression to check
     * @return true if the expression is affine, false otherwise
     */
    boolean isAffineExpression(Expression expr);

    @Module
    interface Implementation extends AffineAnalysis {
        @Binding(BindingKind.INJECTED)
        Types types();

        @Binding(BindingKind.INJECTED)
        FreeVariables freeVariables();

        @Override
        default boolean isAffineIterationScope(StmtForeach foreach) {
            // Check if the generator collection is affine
            if (!isAffineCollection(foreach.getGenerator().getCollection())) {
                return false;
            }

            // Check if all filters are affine
            for (Expression filter : foreach.getFilters()) {
                if (!isAffineExpression(filter)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        default boolean isAffine(StmtForeach foreach) {
            // Check iteration scope first
            if (!isAffineIterationScope(foreach)) {
                return false;
            }

            // Check the loop body
            if (!isAffineBody(foreach)) {
                return false;
            }

            return true;
        }

        @Override
        default boolean isAffineBody(StmtForeach foreach) {
            // Get variables bound by the loop
            Set<String> boundVariableNames = foreach.getGenerator().getVarDecls().stream()
                    .map(varDecl -> varDecl.getName())
                    .collect(Collectors.toSet());

            // Get variables used in the iteration scope (collection and filters)
            Set<String> iterationScopeVars = freeVariables().freeVariables(foreach.getGenerator().getCollection())
                    .stream()
                    .map(var -> var.getName())
                    .collect(Collectors.toSet());

            for (Expression filter : foreach.getFilters()) {
                iterationScopeVars.addAll(freeVariables().freeVariables(filter).stream()
                        .map(var -> var.getName())
                        .collect(Collectors.toSet()));
            }

            // Remove bound variables (they're defined by the loop itself)
            iterationScopeVars.removeAll(boundVariableNames);

            // Check each statement in the body
            for (se.lth.cs.tycho.ir.stmt.Statement stmt : foreach.getBody()) {
                if (!isAffineStatement(stmt, boundVariableNames, iterationScopeVars)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        default boolean isAffineComprehensionScope(ExprComprehension comprehension) {
            // Check if the generator collection is affine
            if (!isAffineCollection(comprehension.getGenerator().getCollection())) {
                return false;
            }

            // Check if all filters are affine
            for (Expression filter : comprehension.getFilters()) {
                if (!isAffineExpression(filter)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        default boolean isAffineComprehension(ExprComprehension comprehension) {
            // Check iteration scope first
            if (!isAffineComprehensionScope(comprehension)) {
                return false;
            }

            // Check the collection expression being generated
            if (!isAffineExpression(comprehension.getCollection())) {
                return false;
            }

            // Check that the collection expression doesn't use variables from outer scope
            // in a way that would be modified (this is more permissive than foreach body checks
            // since comprehensions are pure expressions)
            Set<String> boundVariableNames = comprehension.getGenerator().getVarDecls().stream()
                    .map(varDecl -> varDecl.getName())
                    .collect(Collectors.toSet());

            // Get variables used in the iteration scope (collection and filters)
            Set<String> iterationScopeVars = freeVariables().freeVariables(comprehension.getGenerator().getCollection())
                    .stream()
                    .map(var -> var.getName())
                    .collect(Collectors.toSet());

            for (Expression filter : comprehension.getFilters()) {
                iterationScopeVars.addAll(freeVariables().freeVariables(filter).stream()
                        .map(var -> var.getName())
                        .collect(Collectors.toSet()));
            }

            // Remove bound variables (they're defined by the comprehension itself)
            iterationScopeVars.removeAll(boundVariableNames);

            // For comprehensions, we just need to verify the generated collection is affine
            // (already checked above) - no side effects possible since it's an expression
            
            return true;
        }

        /**
         * Checks if a statement within a loop body is affine.
         * 
         * @param stmt the statement to check
         * @param loopVars variables bound by the enclosing loop
         * @param iterationScopeVars variables used in the iteration scope (shouldn't be modified)
         * @return true if the statement is affine, false otherwise
         */
        default boolean isAffineStatement(se.lth.cs.tycho.ir.stmt.Statement stmt, 
                                          Set<String> loopVars, 
                                          Set<String> iterationScopeVars) {
            // Assignment statements
            if (stmt instanceof se.lth.cs.tycho.ir.stmt.StmtAssignment) {
                se.lth.cs.tycho.ir.stmt.StmtAssignment assignment = 
                    (se.lth.cs.tycho.ir.stmt.StmtAssignment) stmt;
                
                // Check that the RHS expression is affine
                if (!isAffineExpression(assignment.getExpression())) {
                    return false;
                }
                
                // Check that we're not modifying iteration scope variables
                // (This is a simplified check - would need deeper LValue analysis)
                String lvalueName = getLValueName(assignment.getLValue());
                if (lvalueName != null && iterationScopeVars.contains(lvalueName)) {
                    return false;
                }
                
                // Check that array/indexer accesses on LHS use affine indices
                if (!isAffineLValue(assignment.getLValue())) {
                    return false;
                }
                
                return true;
            }
            
            // Block statements - recursively check all statements
            if (stmt instanceof se.lth.cs.tycho.ir.stmt.StmtBlock) {
                se.lth.cs.tycho.ir.stmt.StmtBlock block = (se.lth.cs.tycho.ir.stmt.StmtBlock) stmt;
                for (se.lth.cs.tycho.ir.stmt.Statement innerStmt : block.getStatements()) {
                    if (!isAffineStatement(innerStmt, loopVars, iterationScopeVars)) {
                        return false;
                    }
                }
                return true;
            }
            
            // Nested foreach - recursively check
            if (stmt instanceof StmtForeach) {
                StmtForeach nestedForeach = (StmtForeach) stmt;
                return isAffine(nestedForeach);
            }
            
            // If statements with affine conditions
            if (stmt instanceof se.lth.cs.tycho.ir.stmt.StmtIf) {
                se.lth.cs.tycho.ir.stmt.StmtIf ifStmt = (se.lth.cs.tycho.ir.stmt.StmtIf) stmt;
                if (!isAffineExpression(ifStmt.getCondition())) {
                    return false;
                }
                for (se.lth.cs.tycho.ir.stmt.Statement thenStmt : ifStmt.getThenBranch()) {
                    if (!isAffineStatement(thenStmt, loopVars, iterationScopeVars)) {
                        return false;
                    }
                }
                for (se.lth.cs.tycho.ir.stmt.Statement elseStmt : ifStmt.getElseBranch()) {
                    if (!isAffineStatement(elseStmt, loopVars, iterationScopeVars)) {
                        return false;
                    }
                }
                return true;
            }
            
            // Conservative: other statement types are considered non-affine
            // (e.g., StmtCall, StmtWrite, StmtRead, StmtWhile, etc.)
            return false;
        }

        /**
         * Helper to extract variable name from an LValue (simplified).
         */
        default String getLValueName(se.lth.cs.tycho.ir.stmt.lvalue.LValue lvalue) {
            if (lvalue instanceof se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable) {
                se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable varLValue = 
                    (se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable) lvalue;
                return varLValue.getVariable().getName();
            }
            // For indexed access, field access, etc., return null
            return null;
        }

        /**
         * Checks if an LValue uses only affine index expressions.
         */
        default boolean isAffineLValue(se.lth.cs.tycho.ir.stmt.lvalue.LValue lvalue) {
            if (lvalue instanceof se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable) {
                // Simple variable access is always affine
                return true;
            }
            
            if (lvalue instanceof se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer) {
                se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer indexer = 
                    (se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer) lvalue;
                // Check that the index expression is affine
                if (!isAffineExpression(indexer.getIndex())) {
                    return false;
                }
                // Recursively check the structure being indexed
                return isAffineLValue(indexer.getStructure());
            }
            
            // Other LValue types (field access, deref, etc.) are conservatively non-affine
            return false;
        }

        /**
         * Checks if a collection expression is affine.
         * Affine collections include:
         * - Literal lists with affine elements
         * - Literal sets with affine elements
         * - Comprehensions with affine generators and filters
         * - Variables (assumed to be affine collections)
         */
        default boolean isAffineCollection(Expression collection) {
            if (collection instanceof ExprList) {
                ExprList list = (ExprList) collection;
                // A literal list is affine if all its elements are affine
                return list.getElements().stream().allMatch(this::isAffineExpression);
            } else if (collection instanceof ExprSet) {
                ExprSet set = (ExprSet) collection;
                // A literal set is affine if all its elements are affine
                return set.getElements().stream().allMatch(this::isAffineExpression);
            } else if (collection instanceof ExprComprehension) {
                ExprComprehension comp = (ExprComprehension) collection;
                // Use the comprehensive comprehension check
                return isAffineComprehension(comp);
            } else if (collection instanceof ExprVariable) {
                // Variables are assumed to be affine collections
                return true;
            }
            // Other collection types (function calls, etc.) might be affine,
            // but we conservatively return false
            return false;
        }

        @Override
        default boolean isAffineExpression(Expression expr) {
            // Base case: literals are affine
            if (expr instanceof ExprLiteral) {
                return true;
            }

            // Variables are affine
            if (expr instanceof ExprVariable) {
                return true;
            }

            // Binary operations
            if (expr instanceof ExprBinaryOp) {
                ExprBinaryOp binOp = (ExprBinaryOp) expr;
                
                // For multi-operation expressions like a+b*c, we need to check all operations
                // are affine-preserving, and handle multiplication specially
                for (int i = 0; i < binOp.getOperations().size(); i++) {
                    String op = binOp.getOperations().get(i);
                    
                    // Multiplication requires at least one operand to be constant
                    if (op.equals("*")) {
                        // For a*b, check if either a or b is constant
                        Expression left = binOp.getOperands().get(i);
                        Expression right = binOp.getOperands().get(i + 1);
                        if (!isConstantExpression(left) && !isConstantExpression(right)) {
                            return false;
                        }
                    } else if (!isAffineOperator(op)) {
                        // Non-affine operator found
                        return false;
                    }
                }
                
                // All operations are affine-compatible, check all operands are affine
                return binOp.getOperands().stream().allMatch(this::isAffineExpression);
            }

            // Unary operations
            if (expr instanceof ExprUnaryOp) {
                ExprUnaryOp unOp = (ExprUnaryOp) expr;
                String op = unOp.getOperation();
                
                // Negation and logical NOT preserve affinity
                if (op.equals("-") || op.equals("+") || op.equals("!") || op.equals("not")) {
                    return isAffineExpression(unOp.getOperand());
                }
                
                return false;
            }

            // Array/indexer access - affine if the index is affine
            if (expr instanceof ExprIndexer) {
                ExprIndexer indexer = (ExprIndexer) expr;
                // Check that the index expression is affine
                if (!isAffineExpression(indexer.getIndex())) {
                    return false;
                }
                // Recursively check the structure being indexed
                return isAffineExpression(indexer.getStructure());
            }

            // List/set comprehensions
            if (expr instanceof ExprComprehension) {
                ExprComprehension comp = (ExprComprehension) expr;
                return isAffineComprehension(comp);
            }

            // List literals
            if (expr instanceof ExprList) {
                ExprList list = (ExprList) expr;
                return list.getElements().stream().allMatch(this::isAffineExpression);
            }

            // Set literals
            if (expr instanceof ExprSet) {
                ExprSet set = (ExprSet) expr;
                return set.getElements().stream().allMatch(this::isAffineExpression);
            }

            // Field access and function calls are not affine in general
            return false;
        }

        /**
         * Checks if an expression is constant (doesn't depend on any variables).
         */
        default boolean isConstantExpression(Expression expr) {
            if (expr instanceof ExprLiteral) {
                return true;
            }
            
            if (expr instanceof ExprBinaryOp) {
                ExprBinaryOp binOp = (ExprBinaryOp) expr;
                return binOp.getOperands().stream().allMatch(this::isConstantExpression);
            }
            
            if (expr instanceof ExprUnaryOp) {
                ExprUnaryOp unOp = (ExprUnaryOp) expr;
                return isConstantExpression(unOp.getOperand());
            }
            
            return false;
        }

        /**
         * Checks if a binary operator preserves affinity (excluding multiplication).
         * Multiplication is handled specially because it requires at least one constant operand.
         */
        default boolean isAffineOperator(String op) {
            // Arithmetic operators that preserve affinity
            if (op.equals("+") || op.equals("-")) {
                return true;
            }
            
            // Comparison operators
            if (op.equals("<") || op.equals("<=") || op.equals(">") || 
                op.equals(">=") || op.equals("==") || op.equals("!=")) {
                return true;
            }
            
            // Logical operators
            if (op.equals("&&") || op.equals("||") || op.equals("and") || op.equals("or")) {
                return true;
            }
            
            // Division, modulo, exponentiation, bitwise ops, etc. are not affine
            return false;
        }
    }
}
