package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.Ports;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This phase will catch array index out of bounds errors in the case where PortArrays are indexed in actions.
 */
public class PostPortArrayEnumerationNameAnalysis implements Phase {
    @Override
    public String getDescription() {
        return "Analyzes name binding for the port objects after Port Arrays have been enumerated.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) {
        CheckNames analysis = MultiJ.from(CheckNames.class)
                .bind("tree").to(task.getModule(TreeShadow.key))
                .bind("ports").to(task.getModule(Ports.key))
                .bind("reporter").to(context.getReporter())
                .instance();
        analysis.check(task);
        return task;
    }

    @Module
    public interface CheckNames {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        Ports ports();

        @Binding(BindingKind.INJECTED)
        Reporter reporter();

        default SourceUnit sourceUnit(IRNode node) {
            return sourceUnit(tree().parent(node));
        }

        default SourceUnit sourceUnit(SourceUnit unit) {
            return unit;
        }

        default void check(IRNode node) {
            checkNames(node);
            node.forEachChild(this::check);
        }

        default void checkNames(IRNode node) {
        }


        default void checkNames(Port port) {
            if (ports().declaration(port) == null) {
                reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Port " + port.getName() + " is not declared" +
                        ".", sourceUnit(port), port));
            }
        }

        // Note by Gareth Callanan:
        //
        // This function checks that all the port names are different within the action InputPatterns and
        // OutputExpressions lists. This is likely to occur when two indexing expressions evaluate to the same value
        // when using array ports. This would result in duplicate port names.
        // eg:  "action X[1]:[x0], X[2/2]:[x1] ==> end" will enumerate to "action X__1__:[x0], X__1__:[x1] ==> end"
        // I am not sure if this function is true name analysis, but it fits here.
        default void checkNames(Action action) {
            // 1. Check the input patterns for duplicate ports.
            // 1.1 Collect the input port names into a single list
            List<String> inputPortNames =
                    action.getInputPatterns().stream() // Convert input patterns to stream
                            .map(pattern -> pattern.getPort().getName())//Extract the port names from the input patterns
                            .collect(Collectors.toList()); //Convert the stream to a list

            // 1.2 Find all duplicate names in the list
            Set<String> duplicateInputPortNames =
                    inputPortNames.stream() // Convert inputPortNames list to a stream
                        .filter(i -> Collections.frequency(inputPortNames, i) > 1) // Extract port names that occur more than once
                        .collect(Collectors.toSet()); // Convert extracted port names to a set

            // 1.3 If duplicates exists, print them as an error
            for (String portName : duplicateInputPortNames) {
                reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Port " + portName + " appears twice in the " +
                        "input pattern list.", sourceUnit(action), action));
            }

            // 2.1 Collect the output port names into a single list
            List<String> outputPortNames =
                    action.getOutputExpressions().stream()
                            .map(expression -> expression.getPort().getName())
                            .collect(Collectors.toList());

            // 2.2 Find all duplicate names in the list
            Set<String> duplicateOutputPortNames =
                    outputPortNames.stream()
                            .filter(i -> Collections.frequency(outputPortNames, i) > 1)
                            .collect(Collectors.toSet());

            // 2.3 If duplicates exists, print them as an error
            for (String portName : duplicateOutputPortNames) {
                reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Port " + portName + " appears twice in the " +
                        "output expression list.", sourceUnit(action), action));
            }
        }

        // This function performs a similar roll to checkNames(Action action) instead it checks that there are no
        // duplicates input ports and the output ports at the actor level instead of the action level.
        default void checkNames(CalActor actor) {
            // 1. Check the input patterns for duplicate ports.
            // 1.1 Collect the input port names into a single list
            List<String> portNames =
                    actor.getInputPorts().stream() // Convert input patterns to stream
                            .map(port -> port.getName())//Extract the port names from the input patterns
                            .collect(Collectors.toList()); //Convert the stream to a list

            // 1.2 Collect the output port names into a single list
            List<String> outputPortNames =
                    actor.getOutputPorts().stream()
                            .map(port -> port.getName())
                            .collect(Collectors.toList());

            // 1.3 Combine the input and output ports together in a single list.
            portNames.addAll(outputPortNames);

            // 2.1 Find all duplicate names in the list
            Set<String> duplicatePortNames =
                    portNames.stream() // Convert portNames list to a stream
                            .filter(i -> Collections.frequency(portNames, i) > 1) // Extract port names that occur more than once
                            .collect(Collectors.toSet()); // Convert extracted duplicate port names to a set

            // 2.2 If duplicates exists, print them as an error
            for (String portName : duplicatePortNames) {
                reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Port " + portName + " appears twice in the " +
                        "port list.", sourceUnit(actor), actor));
            }
        }

    }

}
