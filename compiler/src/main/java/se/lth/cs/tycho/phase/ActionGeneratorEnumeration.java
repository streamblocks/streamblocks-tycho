package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.attribute.VariableScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.ActionCase;
import se.lth.cs.tycho.ir.entity.cal.ActionGeneratorStmt;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.meta.interp.Environment;
import se.lth.cs.tycho.meta.interp.Interpreter;
import se.lth.cs.tycho.meta.interp.op.Binary;
import se.lth.cs.tycho.meta.interp.op.Unary;
import se.lth.cs.tycho.meta.interp.value.Value;
import se.lth.cs.tycho.meta.interp.value.ValueBool;
import se.lth.cs.tycho.meta.interp.value.ValueList;
import se.lth.cs.tycho.meta.interp.value.ValueString;
import se.lth.cs.tycho.meta.interp.value.util.Convert;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.*;

/**
 * @author Gareth Callanan
 * <p>
 * Phase that adds support for action generator statements (ActionGeneratorStmt) within CAL actors.
 * <p>
 * An actor can now have generator statements that modifies the AST to create multiple copies of the constructs that
 * fall within the generator statement (the constructs targeted are mainly action definitions).
 * <p>
 * An example of the generator statement is as follows:
 * <pre>
 *     actor Test () int In[2] ==> Out[2]:
 *          foreach uint i in 0..1 generate // The generate statement
 *              testAction: action In[i]:[token] ==> Out[i]:[token] end
 *          end // Corresponding end for generate statement
 *     end
 * </pre>
 * <p>
 * The generator statement unrolls during compilation and the modifications made results in an AST that represents this
 * code:
 * <pre>
 *     actor Test () int In[2] ==> Out[2]:
 *          testAction_0: action In[0]:[token] ==> Out[0]:[token] end
 *          testAction_1: action In[1]:[token] ==> Out[1]:[token] end
 *     end
 * </pre>
 * <p>
 * This class also does the following:
 * <ul>
 * <li>Allows for nesting of action generator statements
 * <li>Checks for variable scope for when the variable declared in the action generator has the same name as one in
 * the nested statements
 * </ul>
 * <p>
 * NOTE: In principle ActionCase constructs are also supported within action generator statments, however these have
 * not been tested and may produce errors. Tests were not created as the ActionCases were still a bit buggy when this
 * phase was created.
 */
public class ActionGeneratorEnumeration implements Phase {

    @Override
    public String getDescription() {
        return "Enumerate the action generator statements within the actors.";
    }

    // Main method for phase. Takes in the AST from the previous phase, transforms the ActionGeneratorStmts within the
    // AST and passes this transformed AST to the next phase.
    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        // Allows us to access variables computable at compile time
        Interpreter interpreter = MultiJ.from(Interpreter.class)
                .bind("variables").to(task.getModule(VariableDeclarations.key))
                .bind("types").to(task.getModule(TypeScopes.key))
                .bind("unary").to(MultiJ.from(Unary.class).instance())
                .bind("binary").to(MultiJ.from(Binary.class).instance())
                .instance();

        // Transformation that enumerates the action generator statements.
        ActionGeneratorEnumeration.Transformation transformation =
                MultiJ.from(ActionGeneratorEnumeration.Transformation.class)
                        .bind("interpreter").to(interpreter)
                        .bind("varScopes").to(task.getModule(VariableScopes.key))
                        .bind("tree").to(task.getModule(TreeShadow.key))
                        .bind("reporter").to(context.getReporter())
                        .instance();
        try {
            return task.transformChildren(transformation);
        } catch (Exception e) {
            context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR,
                    "Error in ActionGeneratorEnumeration Phase: " + e));
            e.printStackTrace();
            return task;
        }
    }

    /**
     * Transformation used by the ActionGeneratorEnumeration phase to unroll the ActionGeneratorStmts.
     */
    @Module
    interface Transformation extends IRNode.Transformation {
        // This attribute allows us to get the enumerated value of an expression where it is possible to calculate it
        // at compile time.
        @Binding(BindingKind.INJECTED)
        Interpreter interpreter();

        // This attribute allows us to access all the variables declared by a given node. Useful for determining scope.
        @Binding(BindingKind.INJECTED)
        VariableScopes varScopes();

        // Allows us to access parent nodes of a node.
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        // Used for reporting errors and warnings
        @Binding(BindingKind.INJECTED)
        Reporter reporter();

        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        // Applies transformation to CalActor
        default IRNode apply(CalActor actor) {
            // 1. Existing actions and actionCase statements outside of actionGeneratorStatements remain unchanged.
            List<Action> actions = new ArrayList<>();
            actions.addAll(actor.getActions());
            List<ActionCase> actionCases = new ArrayList<>();
            actionCases.addAll(actor.getActionCases());

            // 2. Launch the enumeration of the action generator statments
            // In this function actions and actionCases are modified and these modified values are used when this
            // function returns.
            enumerateActionGenerators(actor.getActionGeneratorStmts(), actions, actionCases, new HashMap<String,
                    Value>());

            // 3. Transform the CAL actor with the newly generate actions and actionCases and clear out the
            // actionGeneratorStmts.
            CalActor newActor = actor.withActions(actions).withActionCases(actionCases)
                    .withActionGeneratorStmts(new ArrayList<>()); // No longer need any action
            // generators
            return newActor;
        }

        /**
         * Function that unrolls a list of actionGeneratorStmts and generates new actions and actionCases from this
         * unrolling.
         * <p>
         * Where applicable, the variables declared by the generators are evaluated and when they are used in the
         * actions and actionCases that lie within the generator, they are substituted with the literal value.
         * <p>
         * actionGeneratorStmts can be nested, so this function is designed to be called recursively tracking all the
         * variables declared in the hierarchy of generator statements.
         * <p>
         * NOTE: This function is based very closely on the default IRNode apply(EntityComprehensionExpr comprEntity)
         * function defined in the TemplateInstantiationPhase within this project.
         *
         * @param actionGeneratorStmts List of action generator statements to unroll.
         * @param actions              Input and output parameter. Input is the list of currently existing actions. This
         *                             function then unrolls the actionGeneratorStmts and generates new actions.
         * @param actionCases          Input and output parameter. Input is the list of currently existing
         *                             actionsCases. This
         *                             function then unrolls the actionGeneratorStmts and generates new actionsCases.
         * @param bindings             A map of all the variables declared in the actionGeneratorStmts above this
         *                             one, needed for
         *                             substituting these variables in actions and actionCases when
         *                             actionGeneratorStmts are nested.
         */
        default void enumerateActionGenerators(ImmutableList<ActionGeneratorStmt> actionGeneratorStmts,
                                               List<Action> actions, List<ActionCase> actionCases,
                                               Map<String, Value> bindings) {
            // 1. Iterate through every actionGeneratorStmt
            for (ActionGeneratorStmt actionGenerator : actionGeneratorStmts) {
                Generator generator = actionGenerator.getGenerator();

                // 1.1. Get the list of values in the collection given to the generator. If this cannot be calculated
                // at compile time, throw an error

                // Value value = interpreter().apply(generator.getCollection());
                // By evaluating the value in the collection using interpreter().eval(..., ...(bindings)) instead of
                // of interpreter.apply(...) as in the commented out line above, we are also able to search the
                // variables declared in the action generators higher up in the nested hierarchy.
                Value value = interpreter().eval(generator.getCollection(), new Environment().with(bindings));
                if (!(value instanceof ValueList)) {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Cannot evaluate " +
                            "EntityComprehensionExpr"));
                }

                // 1.2 Iterate through this list of values
                // (NOTE: Generator statements support declaration of multiple variables. In this case, if N
                // variables are declared, then N values in the collection are assigned per iteration, one to each
                // individual variable)
                ValueList list = (ValueList) value;
                if (list.elements().size() % generator.getVarDecls().size() != 0) {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "The number of values in the" +
                            " list of the ActionGeneratorStmt is not divisible by the number of variables the values " +
                            "are assigned to."));
                }
                for (int i = 0; i < list.elements().size(); i += generator.getVarDecls().size()) {
                    // 1.2.1 Set the bindings in the hashmap based on the expected value of the generator variables
                    Map<String, Value> newBindings = new HashMap<>();
                    for (int j = 0; j < generator.getVarDecls().size(); ++j) {
                        newBindings.put(generator.getVarDecls().get(j).getName(), list.elements().get(i));
                    }

                    // 1.2.2 Combine the new bindings with the ones passed into the function to support action
                    // generator nesting.
                    Map<String, Value> combinedBindings = new HashMap<>(bindings);
                    combinedBindings.putAll(newBindings);
                    Environment envFilters = new Environment().with(combinedBindings);

                    // 1.2.3 Generate all actions, action cases and nested actionGenerators for each iteration of the
                    // actionGenerator. The combined environment is used to substitute variables with literals.
                    if (actionGenerator.getFilters().stream().map(filter -> interpreter().eval(filter, envFilters)).allMatch(v -> (v instanceof ValueBool) && ((ValueBool) v).bool())) {
                        actions.addAll(generateActions(actionGenerator, actionGenerator.getActions(),
                                combinedBindings));
                        actionCases.addAll(generateActionCases(actionGenerator, actionGenerator.getActionCases(),
                                combinedBindings));
                        enumerateActionGenerators(actionGenerator.getActionGeneratorStmts(), actions, actionCases,
                                combinedBindings);
                    }
                }
            }
            // No return statement, rather the updated actions and actionCases input parameters are updated.
        }

        /**
         * Generates the actions for an actionGeneratorStmt once the bindings have been unrolled. Where necessary
         * replace expressions in the actions with literals.
         *
         * @param actionGeneratorStmt The action generator statement that this action is generated from.
         * @param actions             Input list of actions within actionGeneratorStmt that must be generated.
         * @param bindings            The pre-computed variables from the nested actionGeneratorStmts that must be
         *                            replaced with
         *                            literals.
         * @return A list of the generated actions.
         */
        default List<Action> generateActions(ActionGeneratorStmt actionGeneratorStmt, ImmutableList<Action> actions,
                                             Map<String, Value> bindings) {
            List<Action> actionRet = new ArrayList<>();

            // 1. Generate a suffix to add to the name of the actions based on the values of the bindings. This ensures
            // each name is uniquely identifiable.
            String suffix = generateNewNameSuffix(bindings);

            // 2. For each action apply the transformation that converts required variables to literals.
            for (Action action : actions) {
                ActionGeneratorEnumeration.ExpressionTransformation transformation =
                        MultiJ.from(ActionGeneratorEnumeration.ExpressionTransformation.class)
                                .bind("environment").to(new Environment().with(bindings))
                                .bind("varScopes").to(varScopes())
                                .bind("tree").to(tree())
                                .bind("topOfTreeNode").to(actionGeneratorStmt)
                                .bind("reporter").to(reporter())
                                .instance();

                // 2.1 Apply this transformation
                Action newAction = (Action) transformation.apply(action);

                // 2.2 Update the action name with the generated suffix.
                if (action.getTag() != null)
                    newAction = newAction.withTag(appendToTag(action.getTag(), suffix));
                actionRet.add((Action) newAction.deepClone());
            }
            return actionRet;
        }

        /**
         * Generates the actionCases for an actionGeneratorStmt once the bindings have been unrolled. Where necessary
         * replace expressions in the actionCases with literals.
         * <p>
         * Follows similar logic to the generateAction(...) function.
         *
         * @param actionGeneratorStmt The action generator statement that this actionCases is generated from.
         * @param actionCases         Input list of actionCases within actionGeneratorStmt that must be generated.
         * @param bindings            The pre-computed variables from the nested actionGeneratorStmts that must be
         *                            replaced with
         *                            literals.
         * @return A list of the generated actionCases.
         */
        default List<ActionCase> generateActionCases(ActionGeneratorStmt actionGeneratorStmt,
                                                     ImmutableList<ActionCase> actionCases,
                                                     Map<String, Value> bindings) {
            List<ActionCase> actionCasesRet = new ArrayList<>();

            String suffix = generateNewNameSuffix(bindings);
            for (ActionCase actionCase : actionCases) {
                ActionGeneratorEnumeration.ExpressionTransformation transformation =
                        MultiJ.from(ActionGeneratorEnumeration.ExpressionTransformation.class)
                                .bind("environment").to(new Environment().with(bindings))
                                .bind("varScopes").to(varScopes())
                                .bind("tree").to(tree())
                                .bind("topOfTreeNode").to(actionCase)
                                .bind("reporter").to(reporter())
                                .instance();

                Action newActionCase = (Action) transformation.apply(actionCase);
                if (actionCase.getTag() != null)
                    newActionCase = newActionCase.withTag(appendToTag(actionCase.getTag(), suffix));
                actionCasesRet.add((ActionCase) newActionCase.deepClone());
            }

            return actionCasesRet;
        }

        /**
         * Append a suffix to the last dot separated tag in an actor/actionCase object.
         *
         * @param qid    The tag to update.
         * @param suffix The suffix to append to the QID.
         * @return The updated tag.
         */
        default QID appendToTag(QID qid, String suffix) {
            String last = qid.getLast().toString() + suffix;
            return qid.withLast(last);
        }

        /**
         * Generate a suffix from a list of variable bindings.
         * <p>
         * Each binding has a name and a value. The suffix is generated by combining the *values* separated with an
         * underscore. So {var1=1, var2=1.1} will generate the suffix "_1_1p1".
         * <p>
         * Any value with a dot (".") in it will have that dot replaced with a p. All suffixes start with an underscore.
         *
         * @param bindings Map of all the bindings to convert into suffix.
         * @return The generated suffix.
         */
        default String generateNewNameSuffix(Map<String, Value> bindings) {
            String suffix = "";
            for (Value value : bindings.values()) {
                String valString = value.toString().replaceAll("\\.", "p");
                suffix = suffix + "_" + valString;
            }
            return suffix;
        }
    }

    /**
     * Transformation that replaces the variable in the action and action case nodes in an actionGeneratorStmt with
     * the calculated literal values. For example, the i in the code below:
     * <pre>
     *      foreach uint i in 0..0 generate
     *          testAction: action In[i]:[token] ==> Out[i]:[token] end
     *      end
     * </pre>
     * gets replaced in the transformation:
     * <pre>
     *      testAction: action In[0]:[token] ==> Out[0]:[token] end
     * </pre>
     * <p>
     * This transformation also needs to take into account the scope of a variable. If the variable i is declared
     * in the generator statement but also in the variable list of an action, then it must not be replaced with a
     * literal value in all cases (Variable shadowing). For example:
     * <pre>
     *      foreach uint i in 0..0 generate
     *          testAction: action In[i]:[token] ==> Out[i]:[token+i]
     *          var
     *              uint i := 3
     *          end
     *      end
     * </pre>
     * Then the i must only be replaced in the port matrix indexing list:
     * <pre>
     *      testAction: action In[0]:[token] ==> Out[0]:[token+i]
     *      var
     *          uint i := 3
     *      end
     * </pre>
     */
    @Module
    interface ExpressionTransformation extends IRNode.Transformation {
        // This environment contains all the variables declared in the nested generator statements that need to be
        // replaced with literals.
        @Binding(BindingKind.INJECTED)
        Environment environment();

        // This attribute allows us to access all the variables declared by a given node. Useful for determining scope.
        @Binding(BindingKind.INJECTED)
        VariableScopes varScopes();

        // Allows us to access parent nodes of a node.
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        // This node indicates the highest level node in the AST that we must check for variables in a conflicting
        // scope.
        @Binding(BindingKind.INJECTED)
        IRNode topOfTreeNode();

        // Used for reporting errors and warnings
        @Binding(BindingKind.INJECTED)
        Reporter reporter();

        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        // When performing this transformation on an action, if a variable is declared in varDeclBlock of an action then
        // it is in the scope of the port array indexes and repeat expressions too. This is problematic as these
        // expressions need to be evaluated at compile time. To get around this, we transform the tree as normal. Then
        // we disable scope checking and perform the transformation again on the port array index and repeat expression
        // this forces these expressions to have matching values overwritten even a variable with the same name falls
        // within the scope.
        default IRNode apply(Action node) {
            // 1. Performs transformation as normal
            Action retAction = node.transformChildren(this);

            // 2. Transform the port array indexes and repeat expressions only.
            // Sets a dummy value $parentCheck$ to "no" so that nodes further down in the tree know that they must skip
            // the scope check.
            Value valueNo = new ValueString("no");
            environment().put("$parentCheck$", valueNo);
            retAction = retAction.transformPortArraysAndRepeats(this);
            Value valueYes = new ValueString("yes");
            environment().put("$parentCheck$", valueYes);
            return retAction;
        }

        // When we encounter an ExprVariable. We first check a declaration with a higher priority does not take
        // precedence in the scope. If not, then we check if its name is in the environment. If the name is in the
        // environment, this means that this expression must be replaced with a literal.
        default IRNode apply(ExprVariable node) {
            // 1. Check that the parentCheck flag is not set to "no" by the apply(Action node) function, if yes, do not
            // perform this scope checking
            Optional<Value> checkParentValue = environment().get("$parentCheck$");
            boolean dontCheckParents =
                    checkParentValue.isPresent() && ((ValueString) checkParentValue.get()).string().equals("no");

            if (!dontCheckParents) {
                // 2. Scan all declarations along the AST up to the topOffTreeNode() node, if a declaration with the
                // same name exists then this has higher priority and we do not apply the transformation.

                IRNode parentNode = tree().parent(node);
                while (parentNode != null) {
                    // 2.1 Check if a declaration exists at this level, if yes, do not apply transformation
                    for (VarDecl decl : varScopes().declarations(parentNode)) {
                        String varDeclName = node.getVariable().getName();
                        if (decl.getName().equals(varDeclName)) {
                            if(environment().get(node.getVariable().getName()).isPresent()) {
                                reporter().report(new Diagnostic(Diagnostic.Kind.WARNING, "Variable " + node.getVariable().getName() + " in a generator statement is shadowed.", sourceUnit(node.getVariable()), node.getVariable()));
                            }
                            return node;
                        }
                    }

                    // 2.2 If no declaration exists, continue traversing up the AST until we reach the topOfTreeNode()
                    // at which point we know that no declaration within the scope was made.
                    parentNode = tree().parent(parentNode);
                    if (parentNode == topOfTreeNode())
                        break;
                }
            }

            // 3. Check if the variable exists in the environment, if yes, replace it with a literal and return the
            // literal as the new expression.
            Convert convert = MultiJ.from(Convert.class).instance();
            Optional<Value> optVal = environment().get(node.getVariable().getName());
            if (optVal.isPresent()) {
                Value value = optVal.get();
                Expression literal = convert.apply(value); // This convert function chould generate a literal.
                return literal;
            }

            // 4. If the variable does not exist in the environment, then return this node untransformed.
            return node;
        }

        default SourceUnit sourceUnit(IRNode node) {
            return sourceUnit(tree().parent(node));
        }

        default SourceUnit sourceUnit(SourceUnit unit) {
            return unit;
        }

    }

}
