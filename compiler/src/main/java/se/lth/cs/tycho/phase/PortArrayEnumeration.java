package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.type.IntType;
import se.lth.cs.tycho.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

/**
 * @author Gareth Callanan
 * <p>
 * Phase that converts a PortDecl AST node representing an array of ports to individual PortDecl nodes for each actor.
 * Additionally, InputPatterns and OutputExpressions are modified so that their indexed ports match the changes to the
 * PortDecl noddes
 * <p>
 * One PortDecl is created for each element in the PortDecl array. By modifying the AST, we do not need to make any
 * modifications to the backend. For example, this:
 * <pre>    actor ActorName int X[3] ==> :</pre>
 * has its AST transformed to be equivalent of this:
 * <pre>    actor ActorName int X__0__, int X__1__, int X__2__ ==> :</pre>
 * <p>
 * Additionally, the OutputExpressions and InputPatterns that index these port arrays need to transform so that the
 * index is now part of the name. for example:
 * <pre>    testAction: action X[0]:[a] ==> : end</pre>
 * has its AST transformed to be equivalent of this:
 * <pre>    testAction: action X__0__:[a] ==> : end</pre>
 * <p>
 * A complete example of this AST transformation:
 * We have the following input code:
 * <pre>
 * actor PassThrough () int(size=8) In[2] ==> int(size=8) Out[2]:
 * 	    testAction: action In[0]:[a], In[1]:[b] ==> Out[0]:[a], Out[1]:[b]
 *     end
 * end
 * </pre>
 * <p>
 * The AST is then transformed to make the above code look like this:
 * <pre>
 * actor PassThrough() int(size = 8) In__0__, int(size = 8) In__1__ ==> int(size = 8) Out__0__, int(size = 8) Out__1__:
 *    testAction: action In__0__:[a], In__1__:[b] ==> Out__0__:[a], Out__1__:[b]
 *    endaction
 * endactor
 * </pre>
 */
public class PortArrayEnumeration implements Phase {

    @Override
    public String getDescription() {
        return "Enumerate all the array ports and convert them AST nodes that can be compiled further.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Transformation transformation = MultiJ.from(Transformation.class)
                .bind("types").to(task.getModule(Types.key))
                .bind("constants").to(task.getModule(ConstantEvaluator.key))
                .bind("treeShadow").to(task.getModule(TreeShadow.key))
                .instance();
        try {
            return task.transformChildren(transformation);
        }catch(Exception e){
            context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR,
                    "Error in PortArrayEnumeration Phase: " + e.toString()));
            e.printStackTrace();
            return task;
        }
    }

    @Module
    interface Transformation extends IRNode.Transformation {

        // This attribute allows access to the type of an expression. Used for checking the type of the expression
        // in the PortyDecl.
        @Binding(BindingKind.INJECTED)
        Types types();

        // This attribute allows us to get the enumerated value of an expression where it is possible to calculate it
        // at compile time. This allows us to access the calculated value of the array initializer expression.
        @Binding(BindingKind.INJECTED)
        ConstantEvaluator constants();

        // This attribute allows us to get the parent of a node.
        @Binding(BindingKind.INJECTED)
        TreeShadow treeShadow();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        /**
         * Function that takes every PortDecl object attached to an actor node, enumerates its array initialisation
         * expression if it exists and generates the PortDecl equal to the enumerated value.
         * <p>
         * The old PortDecl objects are removed from the AST and replaced with the new generated PortDecl objects.
         */
        default IRNode apply(CalActor actor) {
            String entityParentName = ((GlobalEntityDecl) treeShadow().parent(actor)).getName();
            // 1. Expand the input ports
            List<PortDecl> expandedInputPortArrays = enumeratePortArray(actor.getInputPorts(), entityParentName);

            // 2. Expand the output ports as was done in the input ports
            List<PortDecl> expandedOutputPortArrays = enumeratePortArray(actor.getOutputPorts(), entityParentName);

            CalActor newActor = actor.withInputPorts(expandedInputPortArrays).withOutputPorts(expandedOutputPortArrays);
            // We need to transform the children as they contain the actions with the InputPatterns that index arrays
            newActor = newActor.transformChildren(this);
            return newActor;
        }

        /**
         * Function that takes every InputPattern object with an array index, calculates its index gives it a new name
         * to reflect this index. This name should match the names generated for the corresponding PortDecl objects in
         * "IRNode apply(CalActor actor)".
         */
        default IRNode apply(InputPattern inputPattern) {
            Action action = (Action) treeShadow().parent(inputPattern);
            // 1. If the InputPattern object is of child type PortArrayInputPattern, then we need to transform its port
            // name
            if(inputPattern.getArrayIndexExpression() != null){
                // 1.1 We get the value of the index expression
                Expression expr = inputPattern.getArrayIndexExpression();
                long exprValue = evaluateArrayIndexExpr(expr, action, inputPattern.getPort().getName());

                // 1.2 Once we have the value of the expression we need to change the port name to include the index.
                // This new name should equal: <port_array_name>__<array_index>__
                String portName = generatePortNameWithIndex(inputPattern.getPort().getName(), exprValue);
                Port newPort = new Port(portName);
                InputPattern newInputPattern = inputPattern.withPortNoIndexExpression(newPort);

                // 1.3 We can now return
                return newInputPattern;
            } else {
                // 2. If not an instance of PortArrayInputPattern, then no changes are made.
                return inputPattern;
            }
        }

        /**
         * Function that takes every OutputExpression object with an array index, calculates its index gives it a new
         * name to reflect this index. This name should match the names generated for the corresponding PortDecl objects
         * in "IRNode apply(CalActor actor)".
         */
        default IRNode apply(OutputExpression outputExpression) {
            // 1. If the OutputExpression has an array index expression, then we need to transform
            // its port name
            if(outputExpression.getArrayIndexExpression() != null){
                // 1.1 We get the value of the index expression
                Expression expr = outputExpression.getArrayIndexExpression();
                Action action = (Action) treeShadow().parent(outputExpression);
                long exprValue = evaluateArrayIndexExpr(expr, action, outputExpression.getPort().getName());


                // 1.2 Once we have the value of the expression we need to change the port name to include the index.
                // This new name should equal: <port_array_name>__<array_index>__
                String portName = generatePortNameWithIndex(outputExpression.getPort().getName(), exprValue);
                Port newPort = new Port(portName);
                OutputExpression newOutputExpression = outputExpression.withPortNoIndexExpression(newPort);

                return newOutputExpression;
            } else {
                // 2. If not an instance of PortArrayOutputExpression, then no changes are made.
                return outputExpression;
            }
        }

        /**
         * Function that takes every PortDecl object attached to a network node. If it has an array initialisation
         * expression, the function enumerates this expression and generates the PortDecl equal to the enumerated
         * value.
         * <p>
         * The old objects are removed and replaced with these new ports
         */
        default IRNode apply(NlNetwork network) {
            String entityParentName = ((GlobalEntityDecl) treeShadow().parent(network)).getName();
            // 1. Expand the input ports
            List<PortDecl> expandedInputPortArrays = enumeratePortArray(network.getInputPorts(), entityParentName);

            // 2. Expand the output ports as was done in the input ports
            List<PortDecl> expandedOutputPortArrays = enumeratePortArray(network.getOutputPorts(), entityParentName);

            NlNetwork newNetwork = network
                    .withInputPorts(expandedInputPortArrays)
                    .withOutputPorts(expandedOutputPortArrays);
            return newNetwork;
        }

        /**
         * @param ports      The list of ports that need to be enumerated.
         * @param entityName Name of the entity that these ports are attached to
         * @return A list of enumerated ports.
         * @author Gareth Callanan
         * <p>
         * Function that enumerates POrtDecl objects that represent an array of ports.
         * <p>
         * The function takes in a list of PortDecl objects. If a port in the list has an array initialisation
         * expression, then this function takes this array of ports and converts them into individual PortDecl objects
         * with unique names.
         * <p>
         * Additionally, performs error checks on the array initialisation expression to ensure that valid values that
         * can be calculated at compile time are given.
         */
        default List<PortDecl> enumeratePortArray(ImmutableList<PortDecl> ports, String entityName) {
            List<PortDecl> expandedPortList = new ArrayList<>();

            // 1 Iterate through all input ports
            for (PortDecl portDecl : ports) {
                // 1.1 If the port has an array index expression perform enumeration
                if (portDecl.getArrayInitExpr() != null) {
                    // 1.1.1 We need to get the actual value of the expression and confirm that it
                    // is 1) an integer value type is 2) greater than 0
                    Expression expr = portDecl.getArrayInitExpr();
                    Type type = types().type(expr);
                    if(type == null || !(type instanceof IntType)){
                        throw new RuntimeException("For PortArray '" + portDecl.getName() + "' of entity '" + entityName
                                + "' we got an index of type '" + type.toString() + "' where we expected type uint or int.");
                    }

                    OptionalLong exprValueOpt = constants().intValue(expr);
                    if(!exprValueOpt.isPresent()){
                        throw new RuntimeException("For PortArray '" + portDecl.getName() + "' of entity '" + entityName
                                + "' we got no value in the ArrayInitExpr. This happens if the index value cannot be evaluated at compile time");
                    }

                    long exprValue = exprValueOpt.getAsLong();
                    if(exprValue <= 0){
                        throw new RuntimeException("For PortArray '" + portDecl.getName() + "' of entity '" + entityName
                                + "' we got an array size of '" + exprValue + "'. We expect a value greater than 0.");
                    }

                    // 1.1.2 Once we have the value of the expression we enumerate the ports
                    for (int i = 0; i < exprValue; i++) {// Need to replace this with the evaluated expression
                        String portName = generatePortNameWithIndex(portDecl.getName(), i);
                        PortDecl newPort = new PortDecl(portName,(TypeExpr) portDecl.getType().deepClone());
                        expandedPortList.add(newPort);
                    }
                } else {
                    expandedPortList.add(portDecl.clone());
                }
            }

            return expandedPortList;
        }

        /**
         * Returns the value of the array index expression for an InputPattern or OutputExpression node or throws an
         * error if the value of the expression is not as expected.
         *
         * @throws RuntimeException Thrown if the given expression is of the incorrect type, cannot be evaluated at
         * compile time or is less than zero.
         *
         * @param arrayIndexExpr The expression representing the array index within the array of ports.
         * @param action The action that the InputPattern or OutputExpression is part of. Used to produce meaningful
         *              error messages.
         * @param portName The port that the InputPattern or OutputExpression is part of. Used to produce meaningful
         *                 error messages.
         * @return Returns the value of the array index expression.
         */
        default long evaluateArrayIndexExpr(Expression arrayIndexExpr, Action action, String portName) {
            Type type = types().type(arrayIndexExpr);
            if (type == null || !(type instanceof IntType)) {
                // The actor name is 3 entities up in the AST: GlobalEntityDecl -> Actor -> Action -> OutputExpression
                GlobalEntityDecl actorParent = (GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action));
                throw new RuntimeException("For PortArray '" + portName + "' of actor::action '"
                        + actorParent.getName() + "::" + action.getTag() +
                        "' we got an index of type '" + type.toString() + "' where we expected type uint or int.");
            }
            OptionalLong exprValueOpt = constants().intValue(arrayIndexExpr);
            if (!exprValueOpt.isPresent()) {
                GlobalEntityDecl actorParent = (GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action));
                throw new RuntimeException("For PortArray '" + portName + "' of actor::action '"
                        + actorParent.getName() + "::" + action.getTag() +
                        "' we got no value in the ArrayInitExpr. This happens if the index value cannot be evaluated at compile time");
            }
            long exprValue = exprValueOpt.getAsLong();
            if (exprValue < 0) {
                GlobalEntityDecl actorParent = (GlobalEntityDecl) treeShadow().parent(treeShadow().parent(action));
                throw new RuntimeException("For PortArray '" + portName + "' of actor::action '"
                        + actorParent.getName() + "::" + action.getTag() +
                        "' we got an array index of '" + exprValue + "'. We expect a value greater than or equal to 0.");
            }
            return exprValue;
        }
    }

    /**
     * Produces a unique name for a particular port within an array of ports.
     *
     * portName[portIndex] produces portName__portIndex__ as a unique name where portIndex is expected to be an integer
     * greater than or equal to 0.
     *
     * @param portName The name of the array of ports.
     * @param portIndex The index of the port within the array of ports.
     * @return The new name generated from the portIndex and portName.
     */
    public static String generatePortNameWithIndex(String portName, long portIndex){
        return portName + "__" + portIndex + "__";
    }
}