package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.decoration.TypeToTypeExpr;
import se.lth.cs.tycho.ir.*;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.InputVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.Match;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.meta.interp.value.Value;
import se.lth.cs.tycho.meta.interp.value.ValueList;
import se.lth.cs.tycho.meta.interp.value.ValueSet;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.type.*;

import java.util.*;

/**
 * @author Gareth Callanan
 * <p>
 * Phase that transforms a CAL actor action AST. This transformation occurs when the action Input Pattern or
 * Output Expression portIndexExpression is given as a collection of integers instead of a single integer. An example:
 * <pre>    action X[0..2]:[x] ...</pre>
 * This phase evaluates this collection and generates a new InputPattern/OutputExpression for each value in the
 * collection. So for the above example the AST is transformed to instead represent this code:
 * <pre>     action X[0]:[...], X[1]:[...], X[2]:[...]...</pre>
 * For X[0..2]:[x], x must be a list of size 3 with the type of internal values equivalent to the type of the Port X.
 * When the the new input pattern are generated, each X[n] makes up one element within x. We need to introduce a
 * local variable in the AST to match the x and populate it with values from X[n] which now have declare intermediary
 * variables:
 *      <pre>
 *      action X[0]:[x__0__], X[1]:[x__1__], X[2]:[x__2__] ... :
 *      var
 *          List(type:uint(size=8), size=2) x := [x__0__, x__1__, x__2__]
 *      ...
 *      </pre>
 * When the repeat keyword is used, the variable x needs to be a list of lists. So the AST for
 * 'X[0..2]:[x] repeat 2' represents:
 *      <pre>
 *      action X[0]:[x__0__] repeat 2, X[1]:[x__1__] repeat 2, X[2]:[x__2__] repeat 2 ... :
 *      var
 *          List(type: List(type: int(size = 8), size = 2), size = 3) a := [__a0__, __a1__, __a2__]
 *      ...
 *      </pre>
 * The collection given to the portIndexExpression is referred to as a 'Multiport Selector' and the process of
 * transforming the AST is referred to as 'Multiport Selector Expansion'.
 * <p>
 * For output expressions Z[0..2]:[z], z must be a list of size 3 with the type of internal values equivalent to the
 * type of the Port Z. We do not need to generate this value, as it is expected that it should already be declared (as
 * is the case with all output expressions). The AST transformation needs to index z for each port. So the AST
 * for 'Z[0..2]:[z]' should be transformed to represent 'Z[0]:[z[0]], Z[1]:[z[1]], Z[2]:[z[2]]'
 * <p>
 * A complete example of this AST transformation:
 * We have the following input code:
 * <pre>
 * actor Add (uint nPorts) uint(size=8) X[nPorts], uint(size=8) Y ==> uint(size=8) Z[nPorts]:
 *     testAction1: action X[0..nPorts]:[x], Y:[y] ==> Z[0..nPorts]:[z]
 *     var
 *         List(type:uint(size=8), size=nPorts) z
 *     do
 *         foreach uint i in 0..nPorts-1 do
 *                 z[i] := x[i] + y;
 *         end
 *     end
 * end
 * </pre>
 * <p>
 * For nPorts=2, the AST is then transformed to make the above code look like this:
 * <pre>
 * actor Add () uint(size=8) X[2], uint(size=8) Y ==> uint(size=8) Z[2]:
 *     testAction1: action X[0]:[x__0__], X[1]:[x__1__], Y:[y] ==> Z[0]:[z[0]], Z[1]:[z[1]]
 *     var
 *         List(type:uint(size=8), size=2) z,
 *         List(type:uint(size=8), size=2) x := [x__0__, x__1__]
 *     do
 *         foreach uint i in 0..2-1 do
 *             z[i] := x[i] + y;
 *         end
 *     end
 * end
 * </pre>
 */
public class PortArrayCollectionExpansion implements Phase {

    @Override
    public String getDescription() {
        return "When multiple ports in an array of ports are selected using range expressions for action firing conditions, expand the AST to include each individual selected port.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        PortArrayCollectionExpansion.Transformation transformation = MultiJ.from(PortArrayCollectionExpansion.Transformation.class)
                .bind("types").to(task.getModule(Types.key))
                .bind("constants").to(task.getModule(ConstantEvaluator.key))
                .bind("treeShadow").to(task.getModule(TreeShadow.key))
                .instance();
        try {
            return task.transformChildren(transformation);
        } catch (Exception e) {
            context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Error in PortArrayMultiportSelectorExpansion Phase: " + e));
            e.printStackTrace();
            return task;
        }
    }

    @Module
    interface Transformation extends IRNode.Transformation {

        // This attribute allows access to the Type of an expression.
        @Binding(BindingKind.INJECTED)
        Types types();

        // This attribute allows us to get the enumerated value of an expression where it is possible to calculate it
        // at compile time. This allows us to access the calculated value of the array initializer expression.
        @Binding(BindingKind.INJECTED)
        ConstantEvaluator constants();

        // This attribute allows us to get the parent of a node. This is useful for printing out better located error
        // messages.
        @Binding(BindingKind.INJECTED)
        TreeShadow treeShadow();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(Action action) {
            List<LocalVarDecl> localVarDecls = new ArrayList<>();
            localVarDecls.addAll(action.getVarDecls());
            List<InputPattern> inputPatterns = inputPatternsSelectorExpansion(action, localVarDecls);
            List<OutputExpression> outputExpressions = outputExpressionsSelectorExpansion(action);

            return action.withInputPatterns(inputPatterns).withOutputExpressions(outputExpressions).withVarDecls(localVarDecls);
        }

        /**
         * Examines all input pattern objects and for ones with multiport selector, create a new InputPattern AST node
         * for each value in the Multiport Selector. Then generates the local variable to replace the variable that
         * has now been split across multiple input patterns.
         *
         * As described in the Class declaration docs, this: <pre>action X[0..2]:[x]</pre>
         * Becomes the AST equivalent of this:
         * <pre>
         *      action X[0]:[__x0__], X[1]:[__x1__], X[2]:[__x2__] ... :
         *      var
         *          List(type:uint(size=8), size=2) x := [__x0__, __x1__, __x2__]
         *      ...
         * </pre>
         *
         * @param action The action AST node
         * @param localVarDecls This is an input and output object. It contains localVarDecls that are part of the
         *                      action and this method adds new variable decls (the lists that are generated) where
         *                      required.
         * @return A list of the new expanded input patterns.
         */
        default List<InputPattern> inputPatternsSelectorExpansion(Action action, List<LocalVarDecl> localVarDecls) {
            List<InputPattern> inputPatterns = new ArrayList<>();
            for (InputPattern inputPattern : action.getInputPatterns()) {
                Expression arrayIndexExpr = inputPattern.getArrayIndexExpression();
                // 1. If the inputPattern object has no array index expression, then we are only looking at a single
                // port, not an array of ports, and we do not need to proceed further.
                if (arrayIndexExpr != null) {
                    Type type = types().type(arrayIndexExpr);
                    // 2. If the type of the arrayIndexExpr is not a CollectionType, then we assume that only a single
                    // port within the array of ports is selected, so we do not need to proceed further.
                    if (type instanceof CollectionType) {
                        // 3. We now know that we have to perform MultiportSelectorExpansion.
                        // 3.1 Check that our multiport collectors are valid.
                        collectionErrorChecks((CollectionType) type, action);

                        // 3.2 Get the values from the Collection specifying which ports in the array to use.
                        Value value = constants().interpreter().apply(arrayIndexExpr);
                        Collection<Value> elements = getCollectionFromValue(value);
                        if (elements == null) {
                            String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                            throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' the expression in an input pattern cannot be evaluated at compile time.");
                        }

                        // 3.3 For each value in the collection generate a new InputPattern object
                        // Each input pattern object requires a:
                        // 1) Port Type (same as the current inputPattern)
                        // 2) Matches - see comment 3.3.1 below
                        // 3) Repeat Expression (same as the current inputPattern)
                        // 4) Port Index Literal (equal to a value within the collection)
                        // Parts 1), 3) and 4) are easy. Part 4) required some work.
                        Type portType = types().portType(inputPattern.getPort());
                        int repeatCount = getRepeatValue(inputPattern.getRepeatExpr(), action);

                        // The declBuilder object stores all the information to create the new required local variable
                        // list once all the new InputPatterns have been generated.
                        ListDeclBuilder declBuilder = new ListDeclBuilder(portType, repeatCount);
                        for (Value listValue : elements) {
                            String litValue = listValue.toString();
                            ExprLiteral portIndexliteral = new ExprLiteral(ExprLiteral.Kind.Integer, litValue);
                            Port newPort = new Port(inputPattern.getPort().getName());
                            Expression repeatExpression = inputPattern.getRepeatExpr() == null ? null : (Expression) inputPattern.getRepeatExpr().deepClone();
                            ImmutableList.Builder<Match> matches = ImmutableList.builder();

                            // 3.3.1 The match in X[0]:[x] is the variable x.
                            //
                            // Generating the matches is a painful task. They have lots of Alternatives which
                            // are not relevant for the standard actions (rather for the untested ActionCase construct).
                            // For pattern X[0]:[x], the only item that needs modifying is the name of x to __x0__
                            // but in order to do that we need to dig quite deeply into the Match object.
                            //
                            // Additionally, each port can potentially have many matches X[0]:[x, y, z], so we need to
                            // do this for each match. X[0]:[x, y, z] must become X[0]:[__x0__, __y0__, __z0__].
                            for (Match match : inputPattern.getMatches()) {
                                InputVarDecl newDecl = new InputVarDecl();
                                ExprCase oldExp = match.getExpression();
                                Expression newScrutineeExpr = new ExprVariable(Variable.variable(newDecl.getName()));
                                String inputTokenVariableName = "";

                                if (oldExp.getAlternatives().size() != 0) {
                                    ExprCase.Alternative alternative = oldExp.getAlternatives().get(0);
                                    String altenrativeExpressionText = ((ExprLiteral) alternative.getExpression()).getText();
                                    if (altenrativeExpressionText == "True") {
                                        PatternBinding binding = (PatternBinding) alternative.getPattern();
                                        inputTokenVariableName = binding.getDeclaration().getName();
                                    } else {
                                        String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                                        throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' the first ExprCase.Alternative in the AST for an input pattern does not have a value of True for expression. Value is '" + altenrativeExpressionText + "'");
                                    }
                                } else {
                                    String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                                    throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' the ExprCase in the AST for an input pattern has no Alternatives. This value is expected to be greater than 0.");
                                }
                                String newInputTokenVariableName = "__" + inputTokenVariableName + litValue + "__";
                                PatternBinding pattern = new PatternBinding(new PatternVarDecl(newInputTokenVariableName));
                                ExprCase.Alternative alternative = new ExprCase.Alternative(pattern, Collections.emptyList(), new ExprLiteral(ExprLiteral.Kind.True));
                                ExprCase.Alternative otherwise = new ExprCase.Alternative(new PatternWildcard(), Collections.emptyList(), new ExprLiteral(ExprLiteral.Kind.False));
                                ExprCase newCase = new ExprCase(newScrutineeExpr, Arrays.asList(alternative, otherwise));
                                Match newMatch = new Match(newDecl, newCase);
                                matches.add(newMatch);
                                declBuilder.addInputTokenVariableName(inputTokenVariableName, newInputTokenVariableName);
                            }

                            // 3.3.2 Now we have all the information we need to create the new input pattern object.
                            InputPattern newInputPattern = new InputPattern(newPort, matches.build(), repeatExpression, portIndexliteral);
                            inputPatterns.add(newInputPattern);
                        }

                        // 4. We can now construct this local variable declaration and add it to the localVarDecls of the action.
                        localVarDecls.addAll(declBuilder.build());
                    } else {
                        inputPatterns.add(inputPattern);
                    }
                } else {
                    inputPatterns.add(inputPattern);
                }
            }
            return inputPatterns;
        }

        /**
         * Examines all output expressions objects and for ones with multiport selector, create a new OutputExpression
         * AST node for each value in the Multiport Selector.
         *
         * As described in the Class declaration docs, this: <pre>action ==> Z[0..2]:[z]</pre>
         * Becomes the AST equivalent of this:
         * <pre>
         *      action ==> Z[0]:[z[0]], Z[1]:[z[1]]:
         * </pre>
         *
         * @param action The action AST node
         * @return A list of the new expanded output expressions.
         */
        default List<OutputExpression> outputExpressionsSelectorExpansion(Action action) {
            List<OutputExpression> outputExpressions = new ArrayList<>();
            for (OutputExpression outputExpression : action.getOutputExpressions()) {
                Expression arrayIndexExpr = outputExpression.getArrayIndexExpression();
                /// 1. If the outputExpression object has no array index expression, then we are only looking at a
                // single port, not an array of ports, and we do not need to proceed further.
                if (arrayIndexExpr != null) {
                    Type type = types().type(arrayIndexExpr);
                    // 2. If the type of the arrayIndexExpr is not a CollectionType, then we assume that only a single
                    // port within the array of ports is selected, so we do not need to proceed further.
                    if (type instanceof CollectionType) {
                        // 3. We now know that we have to perform MultiportSelectorExpansion.
                        // 3.1 Check that our multiport collectors are valid.
                        collectionErrorChecks((CollectionType) type, action);

                        // 3.2 Get the values from the Collection specifying which ports in the array to use.
                        Value value = constants().interpreter().apply(arrayIndexExpr);
                        Collection<Value> elements = getCollectionFromValue(value);
                        if (elements == null) {
                            String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                            throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' the expression in an input pattern cannot be evaluated at compile time.");
                        }

                        // 3.3 Create new output expression objects.
                        //
                        // For each value in the collection generate a new OutputExpression object
                        // Each output expression object requires a:
                        // 1) Port Type (same as the current inputPattern)
                        // 2) Values - see comment 3.3.1 below
                        // 3) Repeat Expression (same as the current inputPattern)
                        // 4) Port Index Literal (equal to a value within the collection)
                        for (Value listValue : elements) {
                            String litValue = listValue.toString();
                            ExprLiteral portIndexliteral = new ExprLiteral(ExprLiteral.Kind.Integer, litValue);
                            Port newPort = new Port(outputExpression.getPort().getName());
                            Expression repeatExpression = outputExpression.getRepeatExpr() == null ? null : (Expression) outputExpression.getRepeatExpr().deepClone();
                            //Expression repeatExpression = null;

                            // 3.3.1 Deal with values.
                            //
                            // The value in Z:[z] is the variable z. If Z is a range of ports Z[0..2]:[z], then z
                            // must now be a list. Where each element in the list is assigned to a different port in
                            // the array. (remember that z must be declared somewhere in the action)
                            //
                            // z needs to be split across the different ports in the array. This is done by indexing z
                            // for each port. As such Z[0..1]:[z] when fully expanded looks like this:
                            // Z[0][z[0]], Z[1][z[1]], Z[2][z[2]].
                            //
                            // There can be more than one value X[0..2]:[x y z], so we need to modify every value.
                            ImmutableList.Builder<Expression> values = ImmutableList.builder();
                            for (Expression valueExpression : outputExpression.getExpressions()) {
                                Expression indexedExpressionVariable = valueExpression.deepClone(); // Deep clone should work
                                ExprLiteral indexedExpressionLiteral = new ExprLiteral(ExprLiteral.Kind.Integer, litValue);
                                ExprIndexer indexedExpression = new ExprIndexer(indexedExpressionVariable, indexedExpressionLiteral);
                                values.add(indexedExpression);
                            }

                            // 3.3.2 Now we have all the information we need to create the new input pattern object.
                            OutputExpression newOutputExpression = new OutputExpression(newPort, values.build(), repeatExpression, portIndexliteral);
                            outputExpressions.add(newOutputExpression);
                        }
                    } else {
                        outputExpressions.add(outputExpression);
                    }
                } else {
                    outputExpressions.add(outputExpression);
                }
            }

            return  outputExpressions;
        }

        /**
         * Performs error checks on the CollectionType used for the Multiport selection. These checks include:
         * 1. That the collection type is one that makes sense to evaluate (eg: MapTypes do not make sense in this
         * context).
         * 2. That the type of the values in the collection type is of type integer. (You cannot represent a port index
         * with a floating point value.)
         * 3. That the number of items in the set is more than 0. (You cannot select 0 ports.)
         * 4. That the items within the collection can be evaluated at compile time.
         *
         * NOTE: This function is a bit messy as there are no common methods between the different collection types to
         * evaluate the internal type and length. I had to check for each type individually. Future work could be to
         * update the parent CollectionType class with some common methods.
         *
         * @param type   The Collection object that needs to be checked for errors.
         * @param action The name of the action that this multiport selector is attached to. Used for better error
         *               messages.
         */
        default void collectionErrorChecks(CollectionType type, Action action) {
            Type internalType = null;
            OptionalLong lengthOpt = null;

            if (type instanceof RangeType) {
                RangeType range = (RangeType) type;
                internalType = range.getType();
                lengthOpt = range.getLength();
            } else if (type instanceof ListType) {
                ListType list = (ListType) type;
                internalType = list.getElementType();
                lengthOpt = list.getSize();
            } else if (type instanceof SetType) {
                SetType set = (SetType) type;
                internalType = set.getElementType();
                lengthOpt = OptionalLong.of(1); // Sets don't have sizes - we set this to one so the error conditions below are not triggered.
            } else {
                String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' we were unable to evaluate the MultiportSelector as the collection type '" + type.toString() + "' is not currently supported for multiport selectors..");
            }

            // Confirm that the type of the collection is a uint or int
            if (!(internalType instanceof IntType)) {
                String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' we got an index of type '" + internalType.toString() + "' where we expected type uint or int.");
            }

            // Confirm that the length can be evaluated at compile time and that the collection is not empty.
            if (!lengthOpt.isPresent()) {
                String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' we could not calculate the values in the input pattern port selector range expression at compile time.");
            }

            long exprValue = lengthOpt.getAsLong();
            if (exprValue < 0) {
                String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' the length of the range expression '" + exprValue + "'. We expect a value greater than 0.");
            }
        }

        default Collection<Value> getCollectionFromValue(Value inputValue) {
            return null;
        }

        default Collection<Value> getCollectionFromValue(ValueList inputValue) {
            return inputValue.elements();
        }

        default Collection<Value> getCollectionFromValue(ValueSet inputValue) {
            return inputValue.elements();
        }

        /**
         * If a repeat expression exists for an InputPattern/OutputExpression, the value of this expression is
         * evaluated and returned. If no repeat expression exists, -1 is returned instead. -1 does NOT indicate that
         * no tokens are expected, rather that a token variable, represents a single token and not a list of tokens.
         * Example: 'X[0..2]:[x] repeat 2*3' will return the value 6 (the result of 2*3).
         *
         * Additionally, checks will be performed to ensure this value is an integer that can be calculated at compile
         * time.
         *
         * @param repeatExpr The repeat expression to be evaluated.
         * @param action The name of the action that this repeat expression is attached to. Used for better error
         *               messages.
         * @return The value of the repeat expression or '-1' if no repeat expression exists.
         */
        default int getRepeatValue(Expression repeatExpr, Action action) {
            if (repeatExpr != null) {
                Type type = types().type(repeatExpr);
                if (!(type instanceof IntType)) {
                    String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                    throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' we got a repeat value of type '" + type.toString() + "' where we expected type uint or int.");
                }

                // Confirm that the length can be evaluated at compile time and that the collection is not empty.
                OptionalLong exprValueOpt = constants().intValue(repeatExpr);
                if (!exprValueOpt.isPresent()) {
                    String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                    throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' we could not calculate the values in the repeat expression at compile time.");
                }

                long exprValue = exprValueOpt.getAsLong();
                if (exprValue < 0) {
                    String entityParentName = ((GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action))).getName();
                    throw new RuntimeException("For action '" + action.getTag() + "' of entity '" + entityParentName + "' the length of the range expression '" + exprValue + "'. We expect a value greater than or equal to 0.");
                }

                return Math.toIntExact(exprValue);
            }else {
                return -1;
            }
        }
    }


    /**
     * Handles generating the local variable list declarations required when expanding InputPattern MultiPortSelectors
     *
     * When converting <pre>X[0..2]:[x]</pre>
     * to:
     * <pre>
     *      action X[0]:[__x0__], X[1]:[__x1__], X[2]:[__x2__] ... :
     *      var
     *          List(type:uint(size=8), size=3) x := [__x0__, __x1__, __x2__]
     *      ...
     * </pre>
     * , the information for the original variable names x and the new names __x1__ etc is scattered all over the code.
     * This object collects all the information and then generates the local variables with the correct initialisation
     * values at the end.
     *
     * This object can handle generating these values for multiple different ports that are part of an action. So:
     * <pre>action X[0..2]:[x], Y[0..2]:[y] </pre> will generate variables x and y.
     */
    private static class ListDeclBuilder {
        private final Type portType;
        private final int repeatCount;
        private final TreeMap<String, LinkedList<String>> mapping;

        /**
         * Constructs a ListDeclBuilder object.
         *
         * @param portType The type of the port
         * @param repeatCount The repeat count value in InputExpression - set to -1 if there is no repeatExpression
         *                    exists.
         */
        public ListDeclBuilder(Type portType, int repeatCount) {
            this.portType = portType;
            this.repeatCount = repeatCount;
            mapping = new TreeMap<>();
        }

        /**
         * For each InputPattern created, indicate the newName given to the declared variable as well as the old name
         * that it originally had. Eg: For X[0..2]:[x]</pre>, x is the originalName and __x0__, __x1__, __x2__  would
         * have to be added for the new names (each with a separate call to this function).
         *
         * @param originalName The name of the variable declared in the original InputPattern object
         * @param newName The name of the variable declared in the new InputPattern when the InputPattern with the
         *                multiport selector has been evaluated and expanded.
         */
        public void addInputTokenVariableName(String originalName, String newName) {
            if (!mapping.containsKey(originalName)) {
                mapping.put(originalName, new LinkedList<String>());
            }
            mapping.get(originalName).addLast(newName);
        }

        /**
         * Print the hash map of values stored. Used for debugging purposes.
         */
        public void print() {
            for (Map.Entry<String, LinkedList<String>> entry : mapping.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue().toString() + " " + entry.getValue().getFirst());
            }
        }

        /**
         * Constructs and returns the local variables from the information provided by the addInputTokenVariableName()
         * function.
         *
         * Each local variable is a list object with initial values: So <pre>X[0..2]:[x]</pre> returns:
         * <pre>List(type:uint(size=8), size=3) x := [__x0__, __x1__, __x2__]</pre>
         * assuming the type of the port is of type uint(size = 8).
         *
         * However, if there is a repeat expression there needs to be a nested list, one for the MultiPortSelector:
         * <pre>List(type: List(type: uint(size = 8), size = 2), size = 3) := [__x0__, __x1__, __x2__]</pre>
         * (assuming repeat value of 2 and port type of integer)
         *
         * @return List of new LocalVarDecls to add to the action.
         */
        public List<LocalVarDecl> build() {
            ArrayList<LocalVarDecl> ret = new ArrayList<>(mapping.size());
            for (Map.Entry<String, LinkedList<String>> entry : mapping.entrySet()) {
                String varName = entry.getKey();

                TypeParameter internalType;
                TypeParameter dataType = new TypeParameter("type", TypeToTypeExpr.convert(portType));
                if (repeatCount == -1) { // -1 indicates that there is no repeat expression, this means we only get 1 token on a port, not a list of tokens
                    internalType = dataType;
                } else {
                    TypeExpr internalListType = new NominalTypeExpr("List", ImmutableList.of(dataType), ImmutableList.of(new ValueParameter("size", new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(repeatCount)))));
                    internalType = new TypeParameter("type", internalListType);
                }
                TypeExpr type = new NominalTypeExpr("List", ImmutableList.of(internalType), ImmutableList.of(new ValueParameter("size", new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(entry.getValue().size())))));

                ImmutableList.Builder<Expression> list = ImmutableList.builder();
                for (String listItemName : entry.getValue()) {
                    list.add(new ExprVariable(Variable.variable(listItemName)));
                }
                ExprList matrixList = new ExprList(list.build());

                ret.add(new LocalVarDecl(new ArrayList<>(), type, varName, matrixList, true));
            }
            return ret;
        }
    }
}