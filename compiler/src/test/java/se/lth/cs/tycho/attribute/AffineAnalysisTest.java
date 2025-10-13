package se.lth.cs.tycho.attribute;

import org.junit.Before;
import org.junit.Test;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.GeneratorVarDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Test class for AffineAnalysis attribute.
 * Tests the affine loop analysis for StmtForeach statements using manually constructed IR nodes.
 * 
 * <p>These tests are inspired by the Scale actor which contains a foreach loop that processes
 * 64 elements with indexed array accesses. The Scale actor loop is:
 * <pre>
 * foreach int i in 0 .. 63 do
 *     y[i] := x[i] * Scale_factor[i];
 * end
 * </pre>
 * 
 * <p>For polyhedral loop analysis, what matters is:
 * <ul>
 *   <li>Loop bounds are affine (e.g., 0..63)</li>
 *   <li>Array access indices are affine (e.g., y[i], x[i])</li>
 *   <li>Memory access pattern is predictable</li>
 * </ul>
 * 
 * The RHS computation itself (e.g., x[i] * Scale_factor[i]) can be any arithmetic - 
 * what matters for affine analysis is that the array indices are affine functions of 
 * the loop variables, not that the computed values follow affine arithmetic.
 */
public class AffineAnalysisTest {

    private AffineAnalysis affineAnalysis;

    @Before
    public void setUp() {
        // Create a test implementation that doesn't require the full compilation context
        affineAnalysis = new AffineAnalysis.Implementation() {
            @Override
            public Types types() {
                // Return null - not used in basic affine expression tests
                return null;
            }

            @Override
            public FreeVariables freeVariables() {
                // Return a simplified FreeVariables implementation for testing
                return node -> {
                    java.util.Set<Variable> vars = new java.util.HashSet<>();
                    collectVariables(node, vars);
                    return vars;
                };
            }

            private void collectVariables(se.lth.cs.tycho.ir.IRNode node, java.util.Set<Variable> vars) {
                if (node instanceof ExprVariable) {
                    vars.add(((ExprVariable) node).getVariable());
                } else {
                    node.forEachChild(child -> collectVariables(child, vars));
                }
            }
        };
    }

    /**
     * Test foreach with range and indexed assignment with multiplication.
     * This tests the foreach loop from the Scale actor:
     * <pre>
     * foreach int i in 0 .. 63 do
     *     y[i] := x[i] * Scale_factor[i];
     * end
     * </pre>
     * 
     * This loop IS affine because:
     * - Loop bound is affine: 0..63
     * - All array indices are affine: y[i], x[i], Scale_factor[i]
     * - Memory access pattern is predictable
     * 
     * The RHS computation (x[i] * Scale_factor[i]) can be any arithmetic -
     * for polyhedral optimization, only the indices need to be affine.
     */
    @Test
    public void testForEachWithRangeAndMultiplication() {
        // Create generator: int i in 0 .. 63
        GeneratorVarDecl varDecl = new GeneratorVarDecl("i");
        Expression range = createRangeExpression(0, 63);
        Generator generator = new Generator(null, Collections.singletonList(varDecl), range);

        // Create body: y[i] := x[i] * Scale_factor[i]
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprVariable x = new ExprVariable(Variable.variable("x"));
        ExprVariable scaleFactor = new ExprVariable(Variable.variable("Scale_factor"));
        
        // x[i]
        ExprIndexer xIndexed = new ExprIndexer(x, i);
        
        // Scale_factor[i]
        ExprIndexer scaleFactorIndexed = new ExprIndexer(scaleFactor, i);
        
        // x[i] * Scale_factor[i]
        ExprBinaryOp multiplication = new ExprBinaryOp(
                ImmutableList.of("*"),
                ImmutableList.of(xIndexed, scaleFactorIndexed)
        );
        
        // y[i] := ...
        LValueVariable y = new LValueVariable(Variable.variable("y"));
        LValueIndexer yIndexed = new LValueIndexer(y, i);
        StmtAssignment assignment = new StmtAssignment(yIndexed, multiplication);
        
        // Create foreach statement
        StmtForeach foreach = new StmtForeach(
                generator,
                Collections.emptyList(), // no filters
                Collections.singletonList(assignment)
        );

        // Test: This SHOULD be affine (all memory accesses use affine indices)
        assertTrue("Iteration scope should be affine",
                affineAnalysis.isAffineIterationScope(foreach));
        assertTrue("Body should be affine (all indices are affine)",
                affineAnalysis.isAffineBody(foreach));
        assertTrue("Foreach should be fully affine",
                affineAnalysis.isAffine(foreach));
    }

    /**
     * Test affine foreach with range and affine indexed assignment (using constant multiplication).
     * This is a truly affine version of the loop.
     */
    @Test
    public void testAffineForEachWithRangeAndIndexedAssignment() {
        // Create generator: int i in 0 .. 63
        GeneratorVarDecl varDecl = new GeneratorVarDecl("i");
        Expression range = createRangeExpression(0, 63);
        Generator generator = new Generator(null, Collections.singletonList(varDecl), range);

        // Create body: y[i] := x[i] * 2  (constant multiplication)
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprVariable x = new ExprVariable(Variable.variable("x"));
        
        // x[i]
        ExprIndexer xIndexed = new ExprIndexer(x, i);
        
        // x[i] * 2
        ExprLiteral two = new ExprLiteral(ExprLiteral.Kind.Integer, "2");
        ExprBinaryOp multiplication = new ExprBinaryOp(
                ImmutableList.of("*"),
                ImmutableList.of(xIndexed, two)
        );
        
        // y[i] := ...
        LValueVariable y = new LValueVariable(Variable.variable("y"));
        LValueIndexer yIndexed = new LValueIndexer(y, i);
        StmtAssignment assignment = new StmtAssignment(yIndexed, multiplication);
        
        // Create foreach statement
        StmtForeach foreach = new StmtForeach(
                generator,
                Collections.emptyList(), // no filters
                Collections.singletonList(assignment)
        );

        // Test: This should be fully affine
        assertTrue("Iteration scope should be affine",
                affineAnalysis.isAffineIterationScope(foreach));
        assertTrue("Body should be affine (constant multiplication)",
                affineAnalysis.isAffineBody(foreach));
        assertTrue("Foreach should be fully affine",
                affineAnalysis.isAffine(foreach));
    }

    /**
     * Test affine expression with addition: i + 1
     */
    @Test
    public void testAffineExpressionAddition() {
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprLiteral one = new ExprLiteral(ExprLiteral.Kind.Integer, "1");
        ExprBinaryOp addition = new ExprBinaryOp(
                ImmutableList.of("+"),
                ImmutableList.of(i, one)
        );

        assertTrue("i + 1 should be affine",
                affineAnalysis.isAffineExpression(addition));
    }

    /**
     * Test affine expression with subtraction: i - 1
     */
    @Test
    public void testAffineExpressionSubtraction() {
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprLiteral one = new ExprLiteral(ExprLiteral.Kind.Integer, "1");
        ExprBinaryOp subtraction = new ExprBinaryOp(
                ImmutableList.of("-"),
                ImmutableList.of(i, one)
        );

        assertTrue("i - 1 should be affine",
                affineAnalysis.isAffineExpression(subtraction));
    }

    /**
     * Test affine expression with constant multiplication: i * 2
     */
    @Test
    public void testAffineExpressionConstantMultiplication() {
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprLiteral two = new ExprLiteral(ExprLiteral.Kind.Integer, "2");
        ExprBinaryOp multiplication = new ExprBinaryOp(
                ImmutableList.of("*"),
                ImmutableList.of(i, two)
        );

        assertTrue("i * 2 should be affine (constant multiplication)",
                affineAnalysis.isAffineExpression(multiplication));
    }

    /**
     * Test non-affine expression: i * j (two variables)
     */
    @Test
    public void testNonAffineExpressionVariableMultiplication() {
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprVariable j = new ExprVariable(Variable.variable("j"));
        ExprBinaryOp multiplication = new ExprBinaryOp(
                ImmutableList.of("*"),
                ImmutableList.of(i, j)
        );

        assertFalse("i * j should NOT be affine (two variables)",
                affineAnalysis.isAffineExpression(multiplication));
    }

    /**
     * Test affine expression with comparison: i < 10
     */
    @Test
    public void testAffineExpressionComparison() {
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprLiteral ten = new ExprLiteral(ExprLiteral.Kind.Integer, "10");
        ExprBinaryOp comparison = new ExprBinaryOp(
                ImmutableList.of("<"),
                ImmutableList.of(i, ten)
        );

        assertTrue("i < 10 should be affine",
                affineAnalysis.isAffineExpression(comparison));
    }

    /**
     * Test non-affine expression with division: i / 2
     */
    @Test
    public void testNonAffineExpressionDivision() {
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprLiteral two = new ExprLiteral(ExprLiteral.Kind.Integer, "2");
        ExprBinaryOp division = new ExprBinaryOp(
                ImmutableList.of("/"),
                ImmutableList.of(i, two)
        );

        assertFalse("i / 2 should NOT be affine",
                affineAnalysis.isAffineExpression(division));
    }

    /**
     * Test affine list comprehension: [i : for i in 0..10]
     */
    @Test
    public void testAffineListComprehension() {
        GeneratorVarDecl varDecl = new GeneratorVarDecl("i");
        Expression range = createRangeExpression(0, 10);
        Generator generator = new Generator(null, Collections.singletonList(varDecl), range);
        
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprComprehension comprehension = new ExprComprehension(
                generator,
                Collections.emptyList(), // no filters
                i // collection expression
        );

        assertTrue("List comprehension with affine range should be affine",
                affineAnalysis.isAffineComprehension(comprehension));
        assertTrue("Comprehension scope should be affine",
                affineAnalysis.isAffineComprehensionScope(comprehension));
    }

    /**
     * Test affine foreach with filter: foreach i in 0..10, i < 5 do ... end
     */
    @Test
    public void testAffineForEachWithFilter() {
        GeneratorVarDecl varDecl = new GeneratorVarDecl("i");
        Expression range = createRangeExpression(0, 10);
        Generator generator = new Generator(null, Collections.singletonList(varDecl), range);

        // Filter: i < 5
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprLiteral five = new ExprLiteral(ExprLiteral.Kind.Integer, "5");
        ExprBinaryOp filter = new ExprBinaryOp(
                ImmutableList.of("<"),
                ImmutableList.of(i, five)
        );

        // Simple assignment body: x := 0
        LValueVariable x = new LValueVariable(Variable.variable("x"));
        ExprLiteral zero = new ExprLiteral(ExprLiteral.Kind.Integer, "0");
        StmtAssignment assignment = new StmtAssignment(x, zero);

        StmtForeach foreach = new StmtForeach(
                generator,
                Collections.singletonList(filter),
                Collections.singletonList(assignment)
        );

        assertTrue("Foreach with affine filter should be affine",
                affineAnalysis.isAffine(foreach));
    }

    /**
     * Test foreach with nested affine foreach.
     */
    @Test
    public void testNestedAffineForEach() {
        // Outer loop: foreach i in 0..5
        GeneratorVarDecl outerVarDecl = new GeneratorVarDecl("i");
        Expression outerRange = createRangeExpression(0, 5);
        Generator outerGenerator = new Generator(null, Collections.singletonList(outerVarDecl), outerRange);

        // Inner loop: foreach j in 0..3
        GeneratorVarDecl innerVarDecl = new GeneratorVarDecl("j");
        Expression innerRange = createRangeExpression(0, 3);
        Generator innerGenerator = new Generator(null, Collections.singletonList(innerVarDecl), innerRange);

        // Inner body: a[j] := i + j
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprVariable j = new ExprVariable(Variable.variable("j"));
        ExprBinaryOp addition = new ExprBinaryOp(
                ImmutableList.of("+"),
                ImmutableList.of(i, j)
        );
        LValueVariable a = new LValueVariable(Variable.variable("a"));
        LValueIndexer aIndexed = new LValueIndexer(a, j);
        StmtAssignment innerAssignment = new StmtAssignment(aIndexed, addition);

        StmtForeach innerForeach = new StmtForeach(
                innerGenerator,
                Collections.emptyList(),
                Collections.singletonList(innerAssignment)
        );

        // Outer foreach contains inner foreach
        StmtForeach outerForeach = new StmtForeach(
                outerGenerator,
                Collections.emptyList(),
                Collections.singletonList(innerForeach)
        );

        assertTrue("Inner foreach should be affine",
                affineAnalysis.isAffine(innerForeach));
        assertTrue("Outer foreach with nested affine foreach should be affine",
                affineAnalysis.isAffine(outerForeach));
    }

    /**
     * Test that literals and variables are affine expressions.
     */
    @Test
    public void testBasicAffineExpressions() {
        ExprLiteral literal = new ExprLiteral(ExprLiteral.Kind.Integer, "42");
        ExprVariable variable = new ExprVariable(Variable.variable("x"));

        assertTrue("Literals should be affine",
                affineAnalysis.isAffineExpression(literal));
        assertTrue("Variables should be affine",
                affineAnalysis.isAffineExpression(variable));
    }

    /**
     * Test complex affine expression: 2*i + 3
     */
    @Test
    public void testComplexAffineExpression() {
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprLiteral two = new ExprLiteral(ExprLiteral.Kind.Integer, "2");
        ExprLiteral three = new ExprLiteral(ExprLiteral.Kind.Integer, "3");
        
        // 2 * i
        ExprBinaryOp multiplication = new ExprBinaryOp(
                ImmutableList.of("*"),
                ImmutableList.of(two, i)
        );
        
        // 2*i + 3
        ExprBinaryOp addition = new ExprBinaryOp(
                ImmutableList.of("+"),
                ImmutableList.of(multiplication, three)
        );

        assertTrue("2*i should be affine",
                affineAnalysis.isAffineExpression(multiplication));
        assertTrue("2*i + 3 should be affine",
                affineAnalysis.isAffineExpression(addition));
    }

    /**
     * Test affine foreach with function call containing array access.
     * This tests the Rightshift actor pattern:
     * foreach int i in 0 .. 63 do
     *     y[i] := clip_i32((x[i] >> 13) + 128, 0, 255);
     * end
     * 
     * The function call should not prevent affine detection as long as
     * the array accesses within it use affine indices.
     */
    @Test
    public void testAffineForEachWithFunctionCallContainingArrayAccess() {
        // Create generator: int i in 0 .. 63
        GeneratorVarDecl varDecl = new GeneratorVarDecl("i");
        Expression range = createRangeExpression(0, 63);
        Generator generator = new Generator(null, Collections.singletonList(varDecl), range);

        // Create body: y[i] := clip_i32((x[i] >> 13) + 128, 0, 255)
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprVariable x = new ExprVariable(Variable.variable("x"));
        
        // x[i]
        ExprIndexer xIndexed = new ExprIndexer(x, i);
        
        // x[i] >> 13
        ExprLiteral thirteen = new ExprLiteral(ExprLiteral.Kind.Integer, "13");
        ExprBinaryOp rightShift = new ExprBinaryOp(
                ImmutableList.of(">>"),
                ImmutableList.of(xIndexed, thirteen)
        );
        
        // (x[i] >> 13) + 128
        ExprLiteral onetwentyeight = new ExprLiteral(ExprLiteral.Kind.Integer, "128");
        ExprBinaryOp addition = new ExprBinaryOp(
                ImmutableList.of("+"),
                ImmutableList.of(rightShift, onetwentyeight)
        );
        
        // clip_i32(..., 0, 255)
        ExprVariable clipFunc = new ExprVariable(Variable.variable("clip_i32"));
        ExprLiteral zero = new ExprLiteral(ExprLiteral.Kind.Integer, "0");
        ExprLiteral twofiftyfive = new ExprLiteral(ExprLiteral.Kind.Integer, "255");
        ExprApplication funcCall = new ExprApplication(
                clipFunc,
                ImmutableList.of(addition, zero, twofiftyfive)
        );
        
        // y[i] := ...
        LValueVariable y = new LValueVariable(Variable.variable("y"));
        LValueIndexer yIndexed = new LValueIndexer(y, i);
        StmtAssignment assignment = new StmtAssignment(yIndexed, funcCall);
        
        // Create foreach statement
        StmtForeach foreach = new StmtForeach(
                generator,
                Collections.emptyList(), // no filters
                Collections.singletonList(assignment)
        );

        // Test: This should be affine (function call with affine array access)
        assertTrue("Iteration scope should be affine",
                affineAnalysis.isAffineIterationScope(foreach));
        assertTrue("Body should be affine (function call with affine indices)",
                affineAnalysis.isAffineBody(foreach));
        assertTrue("Foreach with function call should be affine",
                affineAnalysis.isAffine(foreach));
    }

    /**
     * Test that nested array indices (y[x[i]]) are correctly rejected as non-affine.
     * This tests the fix where x[i] in an index position should not be considered affine
     * because the values it returns might not be affine.
     */
    @Test
    public void testNonAffineNestedArrayIndex() {
        // Create generator: int i in 0 .. 10
        GeneratorVarDecl varDecl = new GeneratorVarDecl("i");
        Expression range = createRangeExpression(0, 10);
        Generator generator = new Generator(null, Collections.singletonList(varDecl), range);

        // Create body: z[i] := y[x[i]]  (nested array access)
        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprVariable x = new ExprVariable(Variable.variable("x"));
        ExprVariable y = new ExprVariable(Variable.variable("y"));
        
        // x[i]
        ExprIndexer xIndexed = new ExprIndexer(x, i);
        
        // y[x[i]] - the index is x[i], which is NOT an affine function
        ExprIndexer yNestedIndex = new ExprIndexer(y, xIndexed);
        
        // z[i] := y[x[i]]
        LValueVariable z = new LValueVariable(Variable.variable("z"));
        LValueIndexer zIndexed = new LValueIndexer(z, i);
        StmtAssignment assignment = new StmtAssignment(zIndexed, yNestedIndex);
        
        // Create foreach statement
        StmtForeach foreach = new StmtForeach(
                generator,
                Collections.emptyList(),
                Collections.singletonList(assignment)
        );

        // Test: This should NOT be affine (nested array index)
        assertTrue("Iteration scope should be affine",
                affineAnalysis.isAffineIterationScope(foreach));
        assertFalse("Body should NOT be affine (nested array index x[i])",
                affineAnalysis.isAffineBody(foreach));
        assertFalse("Foreach with nested array index should NOT be affine",
                affineAnalysis.isAffine(foreach));
    }

    /**
     * Test that filters with array accesses are correctly rejected as non-affine.
     * Example: foreach int i in 0..n, x[i] < 10 do ...
     * This should be rejected because x[i] could return non-affine values.
     */
    @Test
    public void testNonAffineFilterWithArrayAccess() {
        // Create generator: int i in 0 .. 10, x[i] < 10
        GeneratorVarDecl varDecl = new GeneratorVarDecl("i");
        Expression range = createRangeExpression(0, 10);
        Generator generator = new Generator(null, Collections.singletonList(varDecl), range);

        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprVariable x = new ExprVariable(Variable.variable("x"));
        ExprIndexer xIndexed = new ExprIndexer(x, i);
        ExprLiteral ten = new ExprLiteral(ExprLiteral.Kind.Integer, "10");
        
        // Filter: x[i] < 10
        Expression filter = new ExprBinaryOp(
                ImmutableList.of("<"),
                ImmutableList.of(xIndexed, ten)
        );

        // Simple body
        LValueVariable y = new LValueVariable(Variable.variable("y"));
        LValueIndexer yIndexed = new LValueIndexer(y, i);
        StmtAssignment assignment = new StmtAssignment(yIndexed, i);
        
        // Create foreach with filter
        StmtForeach foreach = new StmtForeach(
                generator,
                Collections.singletonList(filter),
                Collections.singletonList(assignment)
        );

        // Test: This should NOT be affine (filter uses array access)
        assertFalse("Iteration scope should NOT be affine (filter has array access)",
                affineAnalysis.isAffineIterationScope(foreach));
        assertFalse("Foreach with array access in filter should NOT be affine",
                affineAnalysis.isAffine(foreach));
    }

    /**
     * Test that filters with pure affine expressions are accepted.
     * Example: foreach int i in 0..100, i mod 2 == 0 do ...
     * Note: This would still fail with current implementation since modulo is not affine.
     * Let's test: foreach int i in 0..100, i < 50 do ...
     */
    @Test
    public void testAffineFilterWithPureExpression() {
        // Create generator: int i in 0 .. 100, i < 50
        GeneratorVarDecl varDecl = new GeneratorVarDecl("i");
        Expression range = createRangeExpression(0, 100);
        Generator generator = new Generator(null, Collections.singletonList(varDecl), range);

        ExprVariable i = new ExprVariable(Variable.variable("i"));
        ExprLiteral fifty = new ExprLiteral(ExprLiteral.Kind.Integer, "50");
        
        // Filter: i < 50
        Expression filter = new ExprBinaryOp(
                ImmutableList.of("<"),
                ImmutableList.of(i, fifty)
        );

        // Simple body
        LValueVariable y = new LValueVariable(Variable.variable("y"));
        LValueIndexer yIndexed = new LValueIndexer(y, i);
        StmtAssignment assignment = new StmtAssignment(yIndexed, i);
        
        // Create foreach with filter
        StmtForeach foreach = new StmtForeach(
                generator,
                Collections.singletonList(filter),
                Collections.singletonList(assignment)
        );

        // Test: This SHOULD be affine (filter is pure affine expression)
        assertTrue("Iteration scope should be affine (filter is pure affine)",
                affineAnalysis.isAffineIterationScope(foreach));
        assertTrue("Foreach with affine filter should be affine",
                affineAnalysis.isAffine(foreach));
    }

    // Helper methods

    /**
     * Creates a range expression: start..end
     */
    private Expression createRangeExpression(int start, int end) {
        ExprLiteral startLiteral = new ExprLiteral(ExprLiteral.Kind.Integer, String.valueOf(start));
        ExprLiteral endLiteral = new ExprLiteral(ExprLiteral.Kind.Integer, String.valueOf(end));
        return new ExprBinaryOp(
                ImmutableList.of(".."),
                ImmutableList.of(startLiteral, endLiteral)
        );
    }
}

