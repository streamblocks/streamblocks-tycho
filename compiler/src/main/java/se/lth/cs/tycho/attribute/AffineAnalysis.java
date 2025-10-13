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
 * This analysis determines if loops are affine for polyhedral optimization.
 * 
 * An affine loop is a loop where:
 * 1. ITERATION SCOPE:
 *    - The loop collection is an affine structure (e.g., ranges with affine bounds)
 *    - The loop filters are affine constraints (e.g., i < 10, but NOT x[i] < 10)
 *    - Filters use AND only (OR creates non-convex sets)
 * 
 * 2. LOOP BODY:
 *    - Array/indexer accesses use affine index expressions (e.g., y[i], x[2*i+1])
 *    - Index expressions must be affine FUNCTIONS (e.g., i+1, NOT x[i])
 *    - No modifications to variables used in loop bounds/filters
 *    - No non-affine control flow
 * 
 * IMPORTANT DISTINCTIONS:
 * 
 * - Affine Function: f(x) = ax + b (e.g., i, i+1, 2*i)
 *   Used for: indices, bounds, filters
 *   Examples: i, i+1, 2*i ✓  |  x[i], i*j, i/2 ✗
 * 
 * - Affine Access Pattern: Memory accesses with affine indices
 *   Used for: RHS expressions in assignments
 *   Examples: x[i] * y[i] ✓ (indices are affine)  |  z[x[i]] ✗ (nested access)
 * 
 * EXAMPLES:
 * 
 * Affine:
 *   foreach int i in 0..63 do
 *     y[i] := x[i] * scale[i];  // ✓ All indices (i) are affine
 *   end
 * 
 * Affine with filter:
 *   foreach int i in 0..100, i < 50 && i > 10 do  // ✓ Pure affine constraint
 *     y[i] := x[i];
 *   end
 * 
 * Non-affine (nested index):
 *   foreach int i in 0..n do
 *     y[i] := z[x[i]];  // ✗ x[i] is not an affine function
 *   end
 * 
 * Non-affine (filter with array access):
 *   foreach int i in 0..n, x[i] < 10 do  // ✗ x[i] in filter
 *     y[i] := i;
 *   end
 * 
 * Non-affine (non-convex filter):
 *   foreach int i in 0..100, i < 10 || i > 90 do  // ✗ OR creates non-convex set
 *     y[i] := i;
 *   end
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
            // Filters must be affine functions (not just affine access patterns)
            // because the iteration set must be representable in the polyhedral model
            for (Expression filter : foreach.getFilters()) {
                if (!isAffineFunction(filter)) {
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
            // Filters must be affine functions for polyhedral model
            for (Expression filter : comprehension.getFilters()) {
                if (!isAffineFunction(filter)) {
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
            // For comprehensions, we need the generated values to be from affine functions
            // (not just affine access patterns) because the result set must be affine
            if (!isAffineFunction(comprehension.getCollection())) {
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
                
                // Check that all array accesses in the RHS use affine indices
                // Note: We don't require the entire RHS expression to be affine,
                // only that memory accesses use affine indices
                if (!hasAffineMemoryAccesses(assignment.getExpression())) {
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
            // Conditions must be affine functions (not just affine access patterns)
            // for the control flow to be representable in the polyhedral model
            if (stmt instanceof se.lth.cs.tycho.ir.stmt.StmtIf) {
                se.lth.cs.tycho.ir.stmt.StmtIf ifStmt = (se.lth.cs.tycho.ir.stmt.StmtIf) stmt;
                if (!isAffineFunction(ifStmt.getCondition())) {
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
                // Check that the index expression is an affine function
                // (e.g., i, i+1, 2*i are ok; x[i] is NOT ok because values could be non-affine)
                if (!isAffineFunction(indexer.getIndex())) {
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
                // A literal list is affine if all its elements are affine functions
                return list.getElements().stream().allMatch(this::isAffineFunction);
            } else if (collection instanceof ExprSet) {
                ExprSet set = (ExprSet) collection;
                // A literal set is affine if all its elements are affine functions
                return set.getElements().stream().allMatch(this::isAffineFunction);
            } else if (collection instanceof ExprComprehension) {
                ExprComprehension comp = (ExprComprehension) collection;
                // Use the comprehensive comprehension check
                return isAffineComprehension(comp);
            } else if (collection instanceof ExprVariable) {
                // Variables are assumed to be affine collections
                return true;
            } else if (collection instanceof ExprBinaryOp) {
                ExprBinaryOp binOp = (ExprBinaryOp) collection;
                // Check if this is a range expression (e.g., a..b)
                if (binOp.getOperations().size() == 1 && binOp.getOperations().get(0).equals("..")) {
                    // A range is affine if both bounds are affine functions
                    return binOp.getOperands().stream().allMatch(this::isAffineFunction);
                }
            }
            // Other collection types (function calls, etc.) might be affine,
            // but we conservatively return false
            return false;
        }

        /**
         * Checks if an expression is a mathematical affine function.
         * An affine function has the form f(x) = ax + b (linear relationship).
         * 
         * This is stricter than isAffineExpression:
         * - Variables: affine (e.g., i, j)
         * - Literals: affine (e.g., 5, 10)
         * - Addition/subtraction: affine if operands are affine (e.g., i + 1, j - 2)
         * - Multiplication: affine if one operand is constant (e.g., 2*i, but not i*j)
         * - Array accesses: NOT affine (e.g., x[i] could return arbitrary values)
         * 
         * Use this for:
         * - Loop bounds (e.g., 0..n)
         * - Array indices (e.g., i, i+1, 2*i)
         * - Filter conditions (need affine predicates for polyhedral model)
         * 
         * @param expr the expression to check
         * @return true if the expression is a mathematical affine function
         */
        default boolean isAffineFunction(Expression expr) {
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
                
                // For multi-operation expressions, check all operations are affine-preserving
                for (int i = 0; i < binOp.getOperations().size(); i++) {
                    String op = binOp.getOperations().get(i);
                    
                    // Multiplication requires at least one operand to be constant
                    if (op.equals("*")) {
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
                return binOp.getOperands().stream().allMatch(this::isAffineFunction);
            }

            // Unary operations
            if (expr instanceof ExprUnaryOp) {
                ExprUnaryOp unOp = (ExprUnaryOp) expr;
                String op = unOp.getOperation();
                
                // Negation and logical NOT preserve affinity
                if (op.equals("-") || op.equals("+") || op.equals("!") || op.equals("not")) {
                    return isAffineFunction(unOp.getOperand());
                }
                
                return false;
            }

            // Array/indexer access - NOT affine as a function
            // (the returned values could be arbitrary, not a linear function)
            if (expr instanceof ExprIndexer) {
                return false;
            }

            // List/set literals - could be affine if all elements are affine
            // But for simplicity, we reject them (constants would be accepted as literals)
            if (expr instanceof ExprList || expr instanceof ExprSet) {
                return false;
            }

            // Other expression types are not affine functions
            return false;
        }

        @Override
        default boolean isAffineExpression(Expression expr) {
            // This method now delegates to isAffineFunction for consistent semantics.
            // Previously, this method incorrectly accepted array accesses like x[i]
            // as "affine" even though the values might not be affine.
            // 
            // For polyhedral analysis, we need to distinguish:
            // - Affine functions (this method): i, i+1, 2*i (but NOT x[i])
            // - Affine access patterns (hasAffineMemoryAccesses): checks indices only
            //
            // This method is kept for backward compatibility but now has correct semantics.
            return isAffineFunction(expr);
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
         * Checks if all memory accesses (array indexing) in an expression use affine indices.
         * Unlike isAffineExpression, this allows arbitrary arithmetic operations on the values,
         * as long as the array indices themselves are affine.
         * 
         * This is the correct check for polyhedral loop analysis, where we care about
         * predictable memory access patterns, not about the arithmetic being affine.
         * 
         * @param expr the expression to check
         * @return true if all array/indexer accesses use affine indices
         */
        default boolean hasAffineMemoryAccesses(Expression expr) {
            // Literals have no memory accesses
            if (expr instanceof ExprLiteral) {
                return true;
            }

            // Variables have no memory accesses (just a value)
            if (expr instanceof ExprVariable) {
                return true;
            }

            // Binary operations - recursively check operands
            if (expr instanceof ExprBinaryOp) {
                ExprBinaryOp binOp = (ExprBinaryOp) expr;
                return binOp.getOperands().stream().allMatch(this::hasAffineMemoryAccesses);
            }

            // Unary operations - check the operand
            if (expr instanceof ExprUnaryOp) {
                ExprUnaryOp unOp = (ExprUnaryOp) expr;
                return hasAffineMemoryAccesses(unOp.getOperand());
            }

            // Array/indexer access - THIS is what we care about!
            // The index must be an affine function, and we recursively check the structure
            if (expr instanceof ExprIndexer) {
                ExprIndexer indexer = (ExprIndexer) expr;
                // The index expression must be an affine function (not just any expression)
                // e.g., i, i+1, 2*i are ok; x[i] is NOT ok (could be non-affine values)
                if (!isAffineFunction(indexer.getIndex())) {
                    return false;
                }
                // Recursively check the structure being indexed
                return hasAffineMemoryAccesses(indexer.getStructure());
            }

            // List/set comprehensions - check recursively
            if (expr instanceof ExprComprehension) {
                ExprComprehension comp = (ExprComprehension) expr;
                return isAffineComprehension(comp);
            }

            // List literals - check all elements
            if (expr instanceof ExprList) {
                ExprList list = (ExprList) expr;
                return list.getElements().stream().allMatch(this::hasAffineMemoryAccesses);
            }

            // Set literals - check all elements
            if (expr instanceof ExprSet) {
                ExprSet set = (ExprSet) expr;
                return set.getElements().stream().allMatch(this::hasAffineMemoryAccesses);
            }

            // Function calls - check if all arguments have affine memory accesses
            // Pure functions don't affect memory access patterns, so we only need
            // to verify that the arguments themselves use affine indices
            // Note: This assumes functions are pure (no hidden memory accesses).
            // For impure functions (I/O, global state), a more sophisticated 
            // analysis would be needed.
            if (expr instanceof ExprApplication) {
                ExprApplication app = (ExprApplication) expr;
                // Check the function being called
                if (!hasAffineMemoryAccesses(app.getFunction())) {
                    return false;
                }
                // Check all arguments
                return app.getArgs().stream().allMatch(this::hasAffineMemoryAccesses);
            }

            // For other expression types, conservatively return false
            // (unknown patterns should be rejected for safety)
            return false;
        }

        /**
         * Checks if a binary operator preserves affine constraints.
         * 
         * This is used for filters and conditions in loops. For the polyhedral model,
         * operators must preserve the convexity of iteration sets.
         * 
         * Note: Multiplication is handled specially elsewhere because it requires
         * at least one constant operand to be affine.
         */
        default boolean isAffineOperator(String op) {
            // Arithmetic operators that preserve affinity
            if (op.equals("+") || op.equals("-")) {
                return true;
            }
            
            // Comparison operators define affine half-spaces
            // e.g., "i < 10" defines the half-space {i | i < 10}
            if (op.equals("<") || op.equals("<=") || op.equals(">") || 
                op.equals(">=") || op.equals("==") || op.equals("!=")) {
                return true;
            }
            
            // Logical AND operator: Preserves convexity
            // e.g., "i > 0 && i < 10" defines the convex set {i | 0 < i < 10}
            if (op.equals("&&") || op.equals("and")) {
                return true;
            }
            
            // Logical OR operator: REMOVED - creates non-convex sets
            // e.g., "i < 10 || i > 20" creates {i | i < 10 or i > 20} which is non-convex
            // Standard polyhedral tools require convex iteration spaces
            // If you need OR, consider splitting into multiple loops or using advanced tools
            
            // Division, modulo, exponentiation, bitwise ops, etc. are not affine
            return false;
        }
    }
}
