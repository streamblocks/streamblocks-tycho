/**
 * @author Gareth Callanan
 * <p>
 * Phase that prints out the source code from the AST. WIll be used to ensure
 * backwards compatibility with ORCC.
 * <p>
 * So far runs on a simple Add actor declared in a file called arith.cal
 * """
 * actor Add () uint(size=8) X, uint(size=8) Y ==> uint(size=8) Z :
 * testName: action X:[x1,x2], Y:[y1,y2] ==> Z:[x1 + x2 + y1 + y2] end
 * end
 * """
 * <p>
 * When running tycho set pretty-print flag to on:
 * tychoc --set pretty-print=on --source-path source --target-path target Add
 */

package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceFile;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.*;
import se.lth.cs.tycho.ir.decl.*;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.*;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class PrettyPrint implements Phase {
    public static Setting<Boolean> prettyPrint = new OnOffSetting() {
        @Override
        public String getKey() {
            return "pretty-print";
        }

        @Override
        public String getDescription() {
            return "Pretty prints the source code.";
        }

        @Override
        public Boolean defaultValue(Configuration configuration) {
            return false;
        }
    };

    @Override
    public List<Setting<?>> getPhaseSettings() {
        return Collections.singletonList(prettyPrint);
    }

    @Override
    public String getDescription() {
        return "Pretty print the code.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) {
        // 1. Check that the pretty-print flag is set and if it is, perform pretty print
        if (context.getConfiguration().get(prettyPrint)) {
            System.out.println("Pretty printing...");
            System.out.println("");

            // 2. We create a print stream object for printing the pretty print files to
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os);

            // 3. Create a data object to be passed around the tree. This allows
            // for some persistent storage between different calls of print.
            PrettyPrinterData data = new PrettyPrinterData(ps);
            data.printPostElaboration = false;

            // 4. Create the printer object and bind the data object to it.
            PrettyPrinter printer = MultiJ.from(PrettyPrinter.class)
                    .bind("tree").to(task.getModule(TreeShadow.key))
                    .bind("data").to(data)
                    .instance();

            // 5. Call print on the top level IRNode - which is the CompilationTask
            try {
                printer.print(task);

                // 6. Deal with the print stream
                System.out.println(os.toString("UTF8"));
            } catch (Exception e) {
                context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR,
                        "Error in PrettyPrint Phase: " + e.toString()));
                e.printStackTrace();
            }

            // 6. Force the program to quit as we do not want to progress further in the pipeline after pretty printing.
            System.out.println("Pretty Printing code instead of compiling. No further compilation stages will be " +
                    "evaluated.");
            System.exit(0);
        }
        // 7. A phase must always return a compilation task, in this case we
        // return the received task forward as we made no modifications.
        return task;
    }

    /**
     * Module that defines the print() method to be called through the syntax tree
     *
     * NOTE 1: As far as I can tell, if you have multiple levels of inheritance. i.e:
     * IRNode <- SourceUnit <- SourceFile, then you need to define the function
     * print() for all three. So if you don't create a print(SourceFile node)
     * function then when a SourceFile object is passed in the
     * node.forEachChild(this::print) function then print(IRNode node) will be
     * called instead of print(SourceFile node). No idea why this happens.
     *
     * NOTE 2: This code has really expanded far beyond its original scope. It can now pretty print the code to files
     * and also contains a mechanisms to pretty print the code in ORCC format if the right flags are passed. This ORCC
     * formatted print prints the networks as XML, adds hidden eclipse project files and tries to remove global
     * variables where possible. This necessitates a data().generateOrcc flag which adds some branching to the code.
     * This makes it slightly difficult to read.
     *
     * NOTE 3: This class is not very elegant. The PrettyPrintData class is a bit kludged in and the ORCC print adds
     * messy if statements. It was made in a bit of a rush and as a way to get to know the language. There is room
     * for improvement.
     */
    @Module
    public interface PrettyPrinter {

        @Binding(BindingKind.INJECTED)
        PrettyPrinterData data();

        // Attribute that gives access to parent nodes.
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        // If the IRNode parent method is called, we throw an error as we need
        // to implement the overridden method to do pretty printing.
        default void print(IRNode node) {
            throw new RuntimeException(node.getFromLineNumber() + ": Method print(...) is not overridden for class: " + node.getClass().getCanonicalName() + ". We do not know how to pretty print this.");
            // Here we make the call to all the children

        }

        // This is the main method to call, generally pass a CompilationTask in as it contains the top level node.
        default void print(CompilationTask node) {
            data().entityName = "";

            // Create project directory if we generate files
            if(data().directory != null) {
                try {
                    Files.createDirectories(Paths.get(data().directory));
                } catch (IOException e) {
                    throw new RuntimeException("Could not create directory file: " + data().directory + ". Original " +
                            "message: " + e.getMessage());
                }
            }

            // Print all source units
            for (SourceUnit sourceUnit : node.getSourceUnits()) {
                print(sourceUnit);
            }

            // Print generated networks
            if (data().directory != null) {

                String fileName;
                if (data().generateOrcc) {
                    fileName = data().directory + "/src/GeneratedTopLevelNetwork.xdf";

                    // The orcc eclipse project needs a .classpath and .project file
                    try {
                        PrintStream ps;
                        FileOutputStream file = new FileOutputStream(data().directory + "/.classpath");
                        ps = new PrintStream(file);
                        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<classpath>\n" +
                                "        <classpathentry kind=\"src\" path=\"src\"/>\n" +
                                "        <classpathentry kind=\"output\" path=\"bin\"/>\n" +
                                "</classpath>");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("Could not write file: " + fileName + ". Original message: " + e.getMessage());
                    }

                    try {
                        PrintStream ps;
                        FileOutputStream file = new FileOutputStream(data().directory + "/.project");
                        ps = new PrintStream(file);
                        ps.println("<projectDescription>\n" +
                                "        <name>StreamBlocksProject</name>\n" +
                                "        <comment></comment>\n" +
                                "        <projects>\n" +
                                "        </projects>\n" +
                                "        <buildSpec>\n" +
                                "                <buildCommand>\n" +
                                "                        <name>org.eclipse.xtext.ui.shared.xtextBuilder</name>\n" +
                                "                        <arguments>\n" +
                                "                        </arguments>\n" +
                                "                </buildCommand>\n" +
                                "        </buildSpec>\n" +
                                "        <natures>\n" +
                                "                <nature>net.sf.orcc.core.nature</nature>\n" +
                                "                <nature>org.eclipse.xtext.ui.shared.xtextNature</nature>\n" +
                                "                <nature>org.eclipse.jdt.core.javanature</nature>\n" +
                                "        </natures>\n" +
                                "</projectDescription>\n");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("Could not write file: " + fileName + ". Original message: " + e.getMessage());
                    }
                } else {
                    fileName = data().directory + "/GeneratedTopLevelNetwork.cal";
                }

                try {
                    PrintStream ps;
                    FileOutputStream file = new FileOutputStream(fileName);
                    ps = new PrintStream(file);
                    data().setStream(ps);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Could not write file: " + fileName + ". Original message: " + e.getMessage());
                }
            }

            data().entityName = "TopLevelNetwork";
            if (node.getNetwork() != null) {
                if (!data().generateOrcc)
                    data().getStream().println("// Pretty printing generated network - not attached to a source file.");
                print(node.getNetwork());
            }

        }

        default void print(SourceFile node) {
            if (data().directory != null) {
                String fileName = data().directory + "/" + node.getFile().getFileName();
                try {
                    if (!data().generateOrcc) {// Orcc requires each actor to be in its own file with the same name
                        // as the actor name, so we do not set the printstream just yet
                        PrintStream ps;
                        FileOutputStream file = new FileOutputStream(fileName);
                        ps = new PrintStream(file);
                        data().setStream(ps);
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Could not write file: " + fileName + ". Original message: " + e.getMessage());
                }
            }

            if (!data().generateOrcc) {
                data().getStream().println("// Pretty printed version of: " + node.getFile());
            }
            try {
                print(node.getTree());
            } catch (RuntimeException e) {
                throw new RuntimeException(node.getFile() + ": " + e.getMessage());
            }
            if (!data().generateOrcc) {
                data().getStream().println("");
            }
        }

        default void print(SourceUnit node) {
            if (node.getLocation().equals("<prelude>")) {
                return;
            }

            data().getStream().print(data().spaces);
            data().spacesInc();
            data().getStream().println("Source unit: " + node.getLocation() + " " + node.isSynthetic());
            node.forEachChild(this::print);
            data().spacesDec();
        }

        default void print(CalActor node) {
            // Print actor name
            String entityParentName = ((GlobalEntityDecl) tree().parent(node)).getName();
            data().getStream().print(data().spaces + "actor " + entityParentName);

            // Print parameter list
            data().getStream().print("(");
            if (node.getValueParameters().size() != 0) {
                ParameterVarDecl lastVarParam = node.getValueParameters().get(node.getValueParameters().size() - 1);
                for (ParameterVarDecl varParam : node.getValueParameters()) {
                    print(varParam);
                    if (varParam != lastVarParam) {
                        data().getStream().print(", ");
                    }
                }
            }
            data().getStream().print(") ");

            // Print the input port declaration
            if (node.getInputPorts().size() > 2) {
                data().getStream().println();
            }

            data().spacesInc();
            if (node.getInputPorts().size() != 0) {
                PortDecl lastInputPort = node.getInputPorts().get(node.getInputPorts().size() - 1);
                for (PortDecl inputPort : node.getInputPorts()) {
                    if (node.getInputPorts().size() > 2) {
                        data().getStream().print(data().spaces);
                    }
                    print(inputPort);
                    if (lastInputPort != inputPort) {
                        data().getStream().print(", ");

                        // If there are more than two input ports, print a new one on each line - this makes things
                        // easier to read
                        if (node.getInputPorts().size() > 2) {
                            data().getStream().println();
                        }
                    }
                }
            }


            if (node.getInputPorts().size() > 2) {
                data().getStream().println();
                data().getStream().print(data().spaces + "==>");
                data().getStream().println();
                if (node.getOutputPorts().size() <= 2) {
                    data().getStream().print(data().spaces);
                }
            } else {
                data().getStream().print(" ==> ");
                if (node.getOutputPorts().size() > 2) {
                    data().getStream().println();
                }
            }

            // Print the output port declaration
            if (node.getOutputPorts().size() != 0) {
                PortDecl lastOutputPort = node.getOutputPorts().get(node.getOutputPorts().size() - 1);
                for (PortDecl outputPort : node.getOutputPorts()) {
                    if (node.getOutputPorts().size() > 2) {
                        data().getStream().print(data().spaces);
                    }
                    print(outputPort);
                    if (lastOutputPort != outputPort) {
                        data().getStream().print(", ");
                        // If there are more than two output ports, print a new one on each line - this makes things
                        // easier to read
                        if (node.getOutputPorts().size() > 2) {
                            data().getStream().println();
                        }
                    }
                }
            }
            data().spacesDec();

            data().getStream().println(":");
            data().getStream().println();
            data().spacesInc();

            // If we are genertaing code for an ORCC file, then the global var decls are not supported and need to
            // be declared locally within the actor instead.
            if (data().generateOrcc) {
                data().getStream().println(data().spaces + "// Global declarations made local for CAL ORCC.");
                for (GlobalVarDecl varDecl : data().globalVarDecls) {
                    printVarDecl(varDecl, data());
                }
                data().getStream().println(data().spaces + "// Global declarations end");
                data().getStream().println();
            }

            for (LocalVarDecl varDecl : node.getVarDecls()) {
                printVarDecl(varDecl, data());
            }

            data().getStream().println();

            // Print all the actions
            for (Action action : node.getActions()) {
                print(action);
                data().getStream().println("");
            }

            for (ActionCase actionCase : node.getActionCases()) {
                print(actionCase);
                data().getStream().println("");
            }

            for (ActionGeneratorStmt stmt : node.getActionGeneratorStmts()) {
                print(stmt);
                data().getStream().println("");
            }

            // Print the fsm if one exists
            if (node.getScheduleFSM() != null) {
                print(node.getScheduleFSM());
            }

            data().spacesDec();
            data().getStream().println(data().spaces + "end");
            data().getStream().println();
        }

        default void print(NlNetwork node) {
            // Do not print the NlNetwork node in the printPostElaboration phase.
            // "default void print(Network node)" is printed instead.
            if (data().printPostElaboration) {
                return;
            }

            // Print actor name
            String entityParentName = ((GlobalEntityDecl) tree().parent(node)).getName();
            data().getStream().print(data().spaces + "network " + entityParentName);
            // Print parameter list
            data().getStream().print("(");
            if (node.getValueParameters().size() != 0) {
                ParameterVarDecl lastVarParam = node.getValueParameters().get(node.getValueParameters().size() - 1);
                for (ParameterVarDecl varParam : node.getValueParameters()) {
                    print(varParam);
                    if (varParam != lastVarParam) {
                        data().getStream().print(", ");
                    }
                }
                data().getStream().print(node.getTypeParameters().size());
            }
            data().getStream().print(") ");

            // Print the input port declaration
            if (node.getInputPorts().size() > 2) {
                data().getStream().println();
            }

            data().spacesInc();
            if (node.getInputPorts().size() != 0) {
                PortDecl lastInputPort = node.getInputPorts().get(node.getInputPorts().size() - 1);
                for (PortDecl inputPort : node.getInputPorts()) {
                    if (node.getInputPorts().size() > 2) {
                        data().getStream().print(data().spaces);
                    }
                    print(inputPort);
                    if (lastInputPort != inputPort) {
                        data().getStream().print(", ");

                        // If there are more than two input ports, print a new one on each line - this makes things
                        // easier to read
                        if (node.getInputPorts().size() > 2) {
                            data().getStream().println();
                        }
                    }
                }
            }

            if (node.getInputPorts().size() > 2) {
                data().getStream().println();
                data().getStream().print(data().spaces + "==>");
                data().getStream().println();
                if (node.getOutputPorts().size() <= 2) {
                    data().getStream().print(data().spaces);
                }
            } else {
                data().getStream().print(" ==> ");
            }

            // Print the output port declaration
            if (node.getOutputPorts().size() != 0) {
                PortDecl lastOutputPort = node.getOutputPorts().get(node.getOutputPorts().size() - 1);
                for (PortDecl outputPort : node.getOutputPorts()) {
                    if (node.getOutputPorts().size() > 2) {
                        data().getStream().print(data().spaces);
                    }
                    print(outputPort);
                    if (lastOutputPort != outputPort) {
                        data().getStream().print(", ");
                        // If there are more than two output ports, print a new one on each line - this makes things
                        // easier to read
                        if (node.getOutputPorts().size() > 2) {
                            data().getStream().println();
                        }
                    }
                }
            }
            data().spacesDec();
            data().getStream().println(":");

            // Print all the network local variables
            if (node.getVarDecls().size() != 0) {
                data().getStream().println(data().spaces + "var");
                data().spacesInc();
                for (int i = 0; i < node.getVarDecls().size(); i++) {
                    data().getStream().print(data().spaces);
                    LocalVarDecl localVar = node.getVarDecls().get(i);
                    print(localVar);
                    if (i == node.getVarDecls().size() - 1) {
                        data().getStream().print(";");
                    }
                    data().getStream().println();
                }
                data().spacesDec();
            }

            // Print the entity declarations
            data().getStream().println(data().spaces + "entities");
            data().spacesInc();
            for (InstanceDecl entity : node.getEntities()) {
                print(entity);
            }
            data().spacesDec();

            // Print the structure
            data().getStream().println(data().spaces + "structure");
            data().spacesInc();
            for (StructureStatement stmt : node.getStructure()) {
                print(stmt);
            }
            data().spacesDec();

            data().getStream().println(data().spaces + "end");
            data().getStream().println();
        }

        default void print(InstanceDecl node) {
            data().getStream().print(data().spaces + node.getInstanceName() + " = ");
            print(node.getEntityExpr());
            data().getStream().println(";");
        }

        default void print(EntityExpr node) {
            throw new RuntimeException(node.getFromLineNumber() + ": Method print(...) is not overridden for class: " + node.getClass().getCanonicalName() + ". We do not know how to pretty print this.");
        }

        default void print(EntityInstanceExpr node) {
            print(node.getEntityName());
            data().getStream().print("(");
            // Print any parameters
            for (int i = 0; i < node.getValueParameters().size(); i++) {
                ValueParameter param = node.getValueParameters().get(i);
                print(param);
                if (i != node.getValueParameters().size() - 1) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().print(")");
        }

        default void print(EntityComprehensionExpr node) {
            data().getStream().print("[");
            print(node.getCollection());
            data().getStream().print(": for ");
            print(node.getGenerator());
            boolean firstFilter = true;
            for (Expression expr : node.getFilters()) {
                if (firstFilter) {
                    firstFilter = false;
                } else {
                    data().getStream().print(", ");
                }
                print(expr);
            }
            data().getStream().print("]");
        }

        default void print(EntityListExpr node) {
            data().getStream().print("[");
            for (int i = 0; i < node.getEntityList().size(); i++) {
                EntityExpr expr = node.getEntityList().get(i);
                print(expr);
                if (i != node.getEntityList().size() - 1) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().print("]");
        }

        default void print(EntityReferenceLocal node) {
            data().getStream().print(node.getName());
        }

        default void print(EntityReferenceGlobal node) {
            data().getStream().print(node.getGlobalName());
        }

        default void print(Network node) {
            // Do not print the Network node if before the printPostElaboration phase.
            // "default void print(NlNetwork node)" is printed instead.

            // NOTE: This node should not exist before network elaboration. This check is here to scratch that
            // consistency itch. (Check is also in default void print(NlNetwork node))
            if (!data().printPostElaboration) {
                return;
            }

            if (data().generateOrcc) {
                printFNL(node);
            } else {
                printNL(node);
            }
        }

        default void printFNL(Network node) {
            data().getStream().println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            data().getStream().println("<XDF name=\"" + data().entityName + "\">");

            // 1. Input Ports
            for (PortDecl inputPort : node.getInputPorts()) {
                data().getStream().println("    <Port kind=\"Input\" name=\"" + inputPort.getName() + "\">");
                data().getStream().println("        <Type name=\"" + inputPort.getType() + "\">" + ((NominalTypeExpr) inputPort.getType()).getTypeParameters().size() + " " + inputPort.getType().getClass());
                // Nominal type expressions were the only ones in use when this method was created, so casting should
                // not be a problem (famous last words).
                NominalTypeExpr typeExpr = (NominalTypeExpr) inputPort.getType();
                for (ValueParameter valueParam : typeExpr.getValueParameters()) { //
                    data().getStream().println("            <Entry kind=\"Expr\" name=\"" + valueParam.getName() +
                            "\">");

                    //I expect everything to be a literal as all the parameters should have been evaluated by now.
                    data().getStream().print("                <Expr kind=\"Literal\" literal-kind=\"Integer\" " +
                            "value=\"");
                    print(valueParam.getValue());
                    data().getStream().println("\"/>");
                    data().getStream().println("            </Entry>");
                }
                data().getStream().println("        </Type>");
                data().getStream().println("    </Port>");
            }

            // 2. Output Ports
            for (PortDecl outputPort : node.getOutputPorts()) {
                data().getStream().println("    <Port kind=\"Output\" name=\"" + outputPort.getName() + "\">");
                data().getStream().println("        <Type name=\"" + outputPort.getType() + "\">" + +((NominalTypeExpr) outputPort.getType()).getTypeParameters().size() + " " + outputPort.getType().getClass());
                // Nominal type expressions were the only ones in use when this method was created, so casting should
                // not be a problem (famous last words).
                NominalTypeExpr typeExpr = (NominalTypeExpr) outputPort.getType();
                for (ValueParameter valueParam : typeExpr.getValueParameters()) { //
                    data().getStream().println("            <Entry kind=\"Expr\" name=\"" + valueParam.getName() +
                            "\">");

                    //I expect everything to be a literal as all the parameters should have been evaluated by now.
                    data().getStream().print("                <Expr kind=\"Literal\" literal-kind=\"Integer\" " +
                            "value=\"");
                    print(valueParam.getValue());
                    data().getStream().println("\"/>");
                    data().getStream().println("            </Entry>");
                }
                data().getStream().println("        </Type>");
                data().getStream().println("    </Port>");
            }

            // 3. Entity Declarations
            for (Instance entity : node.getInstances()) {
                data().getStream().println("    <Instance id=\"" + entity.getInstanceName() + "\">");
                data().getStream().println("        <Class name=\"" + entity.getEntityName() + "\"/>");
                data().getStream().println("    </Instance>");
            }

            // 4. Connections
            for (Connection connection : node.getConnections()) {
                String srcPort = connection.getSource().getPort();
                String srcActor = "";
                if (connection.getSource().getInstance().isPresent()) {
                    srcActor = connection.getSource().getInstance().get();
                }
                String dstPort = connection.getTarget().getPort();
                String dstActor = "";
                if (connection.getTarget().getInstance().isPresent()) {
                    dstActor = connection.getTarget().getInstance().get();
                }
                data().getStream().println("    <Connection dst=\"" + dstActor + "\" dst-port=\"" + dstPort + "\" " +
                        "src=\"" + srcActor + "\" src-port=\"" + srcPort + "\"/>");
            }


            data().getStream().println("</XDF>");
        }

        default void printNL(Network node) {
            // Ensure all entities are imported - stored in set so only one of each entity type is reported
            HashSet<String> imports = new HashSet<String>();
            for (Instance entity : node.getInstances()) {
                //print(entity);
                imports.add("import entity " + entity.getEntityName() + ";");
            }
            for (String importLine : imports) {
                data().getStream().println(data().spaces + importLine);
            }
            data().getStream().println();

            // Print actor name
            data().getStream().print(data().spaces + "network " + data().entityName);

            // Print parameter list
            data().getStream().print("(");
            data().getStream().print(") ");

            // Print the input port declaration
            if (node.getInputPorts().size() > 2) {
                data().getStream().println();
            }

            data().spacesInc();
            if (node.getInputPorts().size() != 0) {
                PortDecl lastInputPort = node.getInputPorts().get(node.getInputPorts().size() - 1);
                for (PortDecl inputPort : node.getInputPorts()) {
                    if (node.getInputPorts().size() > 2) {
                        data().getStream().print(data().spaces);
                    }
                    print(inputPort);
                    if (lastInputPort != inputPort) {
                        data().getStream().print(", ");

                        // If there are more than two input ports, print a new one on each line - this makes things
                        // easier to read
                        if (node.getInputPorts().size() > 2) {
                            data().getStream().println();
                        }
                    }
                }
            }

            if (node.getInputPorts().size() > 2) {
                data().getStream().println();
                data().getStream().print(data().spaces + "==>");
                data().getStream().println();
                if (node.getOutputPorts().size() <= 2) {
                    data().getStream().print(data().spaces);
                }
            } else {
                data().getStream().print(" ==> ");
            }

            // Print the output port declaration
            if (node.getOutputPorts().size() != 0) {
                PortDecl lastOutputPort = node.getOutputPorts().get(node.getOutputPorts().size() - 1);
                for (PortDecl outputPort : node.getOutputPorts()) {
                    if (node.getOutputPorts().size() > 2) {
                        data().getStream().print(data().spaces);
                    }
                    print(outputPort);
                    if (lastOutputPort != outputPort) {
                        data().getStream().print(", ");
                        // If there are more than two output ports, print a new one on each line - this makes things
                        // easier to read
                        if (node.getOutputPorts().size() > 2) {
                            data().getStream().println();
                        }
                    }
                }
            }
            data().spacesDec();
            data().getStream().println(":");

            // Print all the network local variables

            // Print the entity declarations
            data().getStream().println(data().spaces + "entities");
            data().spacesInc();
            for (Instance entity : node.getInstances()) {
                data().getStream().println(data().spaces + entity.getInstanceName() + " = " + entity.getEntityName().getLast() + "();");
            }
            data().spacesDec();

            // Print the structure
            data().getStream().println(data().spaces + "structure");
            data().spacesInc();
            for (Connection connection : node.getConnections()) {
                String startConnection = connection.getSource().getPort();
                if (connection.getSource().getInstance().isPresent()) {
                    startConnection = connection.getSource().getInstance().get() + "." + startConnection;
                }
                String endConnection = connection.getTarget().getPort();
                if (connection.getTarget().getInstance().isPresent()) {
                    endConnection = connection.getTarget().getInstance().get() + "." + endConnection;
                }
                data().getStream().println(data().spaces + startConnection + " --> " + endConnection + ";");
            }
            data().spacesDec();

            data().getStream().println(data().spaces + "end");
            data().getStream().println();
        }

        default void print(StructureStatement node) {
            throw new RuntimeException(node.getFromLineNumber() + ": Method print(...) is not overridden for class: " + node.getClass().getCanonicalName() + ". We do not know how to pretty print this.");
        }

        default void print(StructureConnectionStmt node) {
            data().getStream().print(data().spaces);
            print(node.getSrc());
            data().getStream().print(" --> ");
            print(node.getDst());
            data().getStream().println(";");
        }

        default void print(PortReference node) {
            if (node.getEntityName() != null) {
                data().getStream().print(node.getEntityName());
                // If the port is part of an array of entities, then it has an index
                // We then print out the index
                if (node.getEntityIndex().size() != 0) {
                    data().getStream().print("[");
                    print(node.getEntityIndex().get(0));
                    data().getStream().print("]");
                }
                data().getStream().print(".");
            }
            data().getStream().print(node.getPortName());

            if (node.getArrayIndexExpr().size() != 0) {
                data().getStream().print("[");
                print(node.getArrayIndexExpr().get(0));
                data().getStream().print("]");
            }
        }

        default void print(StructureIfStmt node) {
            data().getStream().print(data().spaces + "if ");
            print(node.getCondition());
            data().getStream().println(" then");
            data().spacesInc();
            // It is not compulsory to have statements in the true branch.
            if (node.getTrueStmt().size() != 0) {
                print(node.getTrueStmt().get(0));
            }
            data().spacesDec();

            // It's not compulsory to have a false branch, but the false branch can
            // actually be a list of if else statements linked by getFalseStmt() function so we need to trace
            // through it.
            StructureStatement falseStatment = null;
            if (node.getFalseStmt().size() != 0) {
                falseStatment = node.getFalseStmt().get(0);
            }
            while (falseStatment instanceof StructureIfStmt) {
                StructureIfStmt falseStatmentConverted = (StructureIfStmt) falseStatment;
                data().getStream().print(data().spaces + "elseif ");
                print(falseStatmentConverted.getCondition());
                data().getStream().println(" then");
                data().spacesInc();
                print(falseStatmentConverted.getTrueStmt().get(0));
                data().spacesDec();
                falseStatment = falseStatmentConverted.getFalseStmt().get(0);
            }
            // This will be the final else branch if one exists
            if (falseStatment != null) {
                data().getStream().println(data().spaces + "else");
                data().spacesInc();
                print(falseStatment);
                data().spacesDec();
            }
            // Now we have gone through all the branches
            data().getStream().println(data().spaces + "end");
        }

        default void print(StructureForeachStmt node) {
            data().getStream().print(data().spaces + "foreach ");
            print(node.getGenerator());
            data().getStream().println(" do");
            data().spacesInc();
            for (StructureStatement stmt : node.getStatements()) {
                print(stmt);
            }
            data().spacesDec();
            data().getStream().println(data().spaces + "end");
        }

        default void print(Generator node) {
            if (node.getType() != null) {
                print(node.getType());
                data().getStream().print(" ");
            }

            for (GeneratorVarDecl varDecl : node.getVarDecls()) {
                print(varDecl);
            }
            data().getStream().print(" in ");

            print(node.getCollection());

        }

        default void print(GeneratorVarDecl node) {
            data().getStream().print(node.getName());
        }


        default void print(ParameterVarDecl node) {
            print(node.getType());
            data().getStream().print(" " + node.getName());
            if (node.getValue() != null) {
                data().getStream().print(" := ");
                print(node.getValue());
            }
        }

        default void print(NamespaceDecl node) {
            if (!data().generateOrcc) {
                if (!node.getQID().toString().equals("")) {
                    data().getStream().println(data().spaces + "namespace " + node.getQID().toString() + ":");
                    data().spacesInc();
                }
                node.forEachChild(this::print);
                if (!node.getQID().toString().equals("")) {
                    data().spacesDec();
                    data().getStream().println(data().spaces + "end");
                }
            } else {
                // ORCC projects do not have namespaces, rather everything is a package
                // Additionally, each ORCC actor needs to be in its own file.

                // 1. Create namespace directory
                String directoryName = data().directory + "/src";
                if (!node.getQID().toString().equals("")) {
                    directoryName = directoryName + "/" + node.getQID().toString();
                }
                try {
                    Files.createDirectories(Paths.get(directoryName));
                } catch (IOException e) {
                    throw new RuntimeException("Could not create directory file: " + directoryName + ". Original " +
                            "message: " + e.getMessage());
                }

                // 2. Find all global variables
                data().globalVarDecls = node.getVarDecls();

                // 3. ORCC does not support global functions throw error if detected
                for (GlobalVarDecl decl : node.getVarDecls()) {
                    if (decl.getValue() instanceof ExprLambda || decl.getValue() instanceof ExprProc) {
                        throw new RuntimeException("Project contains global function or procedure in " + directoryName + "/" + node.getQID().toString() + " with name: " + decl.getName() + ". This is not supported in ORCC and so the ORCC files cannot be generated.");
                    }
                }

                // 4. Write the files
                for (GlobalEntityDecl entity : node.getEntityDecls()) {
                    // 4.1. Get the correct filename
                    String fileExtension;
                    if (entity.getEntity() instanceof CalActor) {
                        fileExtension = ".cal";
                    } else {
                        fileExtension = ".xdf";
                        if (entity.getEntity() instanceof NlNetwork) {
                            return; //Do not print NlNetworks, they should be replaced with Network nodes earlier in
                            // compilation
                        }
                    }
                    String fileName = directoryName + "/" + entity.getName() + fileExtension;

                    // 4.2 Set the stream
                    try {
                        if(data().directory != null) {
                            PrintStream ps;
                            FileOutputStream file = new FileOutputStream(fileName);
                            ps = new PrintStream(file);
                            data().setStream(ps);
                        }
                        if (!node.getQID().toString().equals("")) {
                            data().getStream().println(data().spaces + "package " + node.getQID().toString() + ";");
                        }
                        data().getStream().println();

                        // 4.3 Add imports to each actor
                        for (Import importNode : node.getImports()) {
                            print(importNode);
                        }

                        // 4.4 Print the actor
                        print(entity);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("Could not write file: " + fileName + ". Original message: " + e.getMessage());
                    }
                }

            }
        }

        default void print(AbstractDecl node) {
            throw new RuntimeException(node.getFromLineNumber() + ": Method print(...) is not overridden for class: " + node.getClass().getCanonicalName() + ". We do not know how to pretty print this.");
        }

        default void print(PortDecl node) {
            print(node.getType());
            data().getStream().print(" " + node.getName());
            if (node.getArrayInitExpr() != null) {
                data().getStream().print("[");
                print(node.getArrayInitExpr());
                data().getStream().print("]");
            }
        }

        default void print(NominalTypeExpr node) {
            data().getStream().print(node.getName());


            // I think that there is at most only one value param and its optional
            // which is why its stored in a list, the list can be zero
            if (node.getValueParameters().size() > 0) {
                data().getStream().print("(");

                // Certain types have parameters, for example, the List parameter
                // will have each object with a specific type.
                // I think the size of this is also only 1 or 0
                for (TypeParameter typeParam : node.getTypeParameters()) {
                    print(typeParam);
                    data().getStream().print(", ");
                }

                for (ValueParameter valParam : node.getValueParameters()) {
                    print(valParam);
                }
                data().getStream().print(")");
            }
        }

        default void print(TypeParameter node) {
            data().getStream().print(node.getName() + ": ");
            print(node.getValue());
        }

        default void print(ValueParameter node) {
            data().getStream().print(node.getName() + " = ");
            print(node.getValue());
        }

        default void print(Expression node) {
            throw new RuntimeException(node.getFromLineNumber() + ": Method print(...) is not overridden for class: " + node.getClass().getCanonicalName() + ". We do not know how to pretty print this.");
        }

        default void print(ExprLiteral node) {
            data().getStream().print(node.getText());
        }

        default void print(ExprGlobalVariable node) {
            // Variables constructed by the compiler have a $ to be unique, this does not compile
            // so we replace it with __ instead so that it can compile.
            //String name = node.getGlobalName().toString();
            String lastName = node.getGlobalName().getLast().toString();
            if (lastName.indexOf("$") == 0) {
                lastName = lastName.replace(lastName, "__" + lastName.substring(1)); // Could be done in a more
                // simple way, this function has gone through a few iterations
            }
            data().getStream().print("" + lastName);
        }

        default void print(ExprComprehension node) {
            data().getStream().print("[");
            data().noBraces = true;
            print(node.getCollection());
            data().noBraces = false;
            data().getStream().print(": for ");
            print(node.getGenerator());
            data().getStream().print("]");
        }

        default void print(Action node) {
            data().getStream().print(data().spaces);
            // Print the action tag if one exists
            if (!node.getTag().equals("")) {
                data().getStream().print(node.getTag() + ": ");
            }
            data().getStream().print("action ");

            // Print input port patterns
            if (node.getInputPatterns().size() > 2) {
                data().getStream().println();
            }

            data().spacesInc();
            for (int i = 0; i < node.getInputPatterns().size(); i++) {
                InputPattern inputPattern = node.getInputPatterns().get(i);
                if (node.getInputPatterns().size() > 2) {
                    data().getStream().print(data().spaces);
                }
                print(inputPattern);
                if (i != node.getInputPatterns().size() - 1) {
                    data().getStream().print(", ");
                    // If there are more than two input ports, print a new one on each line - this makes things
                    // easier to read
                    if (node.getInputPatterns().size() > 2) {
                        data().getStream().println();
                    }
                }
            }

            // Format the ==> correctly, on its own line if we have enough input ports
            // otherwise its inline
            if (node.getInputPatterns().size() > 2) {
                data().getStream().println();
                data().getStream().print(data().spaces + "==>");
                data().getStream().println();
                if (node.getOutputExpressions().size() <= 2) {
                    data().getStream().print(data().spaces);
                } else {
                    data().getStream().print(" ");
                }
            } else {
                data().getStream().print(" ==> ");
                if (node.getOutputExpressions().size() > 2) {
                    data().getStream().println();
                }
            }


            // Print output port expressions
            for (int i = 0; i < node.getOutputExpressions().size(); i++) {
                OutputExpression outputExpression = node.getOutputExpressions().get(i);
                if (node.getOutputExpressions().size() > 2) {
                    data().getStream().print(data().spaces);
                }
                print(outputExpression);
                if (i != node.getOutputExpressions().size() - 1) {
                    data().getStream().print(", ");
                    // If there are more than two output ports, print a new one on each line - this makes things
                    // easier to read
                    if (node.getOutputExpressions().size() > 2) {
                        data().getStream().println();
                    }
                }
            }

            data().spacesDec();
            data().getStream().println("");

            // Print guards
            if (node.getGuards().size() != 0) {
                data().getStream().println(data().spaces + "guard");
                data().spacesInc();
                Expression lastGuard = node.getGuards().get(node.getGuards().size() - 1);
                for (Expression guard : node.getGuards()) {
                    data().getStream().print(data().spaces);
                    print(guard);
                    if (lastGuard != guard) {
                        data().getStream().print(",");
                    }
                    data().getStream().println("");
                }
                data().spacesDec();
            }


            // Print var decls
            if (node.getVarDecls().size() != 0) {
                data().getStream().println(data().spaces + "var");
                data().spacesInc();
                LocalVarDecl lastLocalVar = node.getVarDecls().get(node.getVarDecls().size() - 1);
                for (LocalVarDecl localVar : node.getVarDecls()) {
                    data().getStream().print(data().spaces);
                    print(localVar);
                    if (lastLocalVar != localVar) {
                        data().getStream().print(",");
                    }
                    data().getStream().println("");
                }
                data().spacesDec();
            }


            // Print body
            if (node.getBody().size() != 0) {
                data().getStream().println(data().spaces + "do");
                data().spacesInc();
                for (Statement stmt : node.getBody()) {
                    print(stmt);
                }
                data().spacesDec();
            }


            data().getStream().println(data().spaces + "end");
        }

        default void print(InputPattern node) {
            print(node.getPort());

            if (node.getArrayIndexExpression() != null) {
                data().getStream().print("[");
                print(node.getArrayIndexExpression());
                data().getStream().print("]");
            }

            data().getStream().print(":[");

            Match lastMatch = node.getMatches().get(node.getMatches().size() - 1);
            for (Match match : node.getMatches()) {
                print(match);
                if (match != lastMatch) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().print("]");
            if (node.getRepeatExpr() != null) {
                data().getStream().print(" repeat ");
                print(node.getRepeatExpr());
            }
        }

        default void print(OutputExpression node) {
            print(node.getPort());

            if (node.getArrayIndexExpression() != null) {
                data().getStream().print("[");
                print(node.getArrayIndexExpression());
                data().getStream().print("]");
            }

            data().getStream().print(":[");
            Expression lastExpr = node.getExpressions().get(node.getExpressions().size() - 1);
            for (Expression expr : node.getExpressions()) {
                print(expr);
                if (expr != lastExpr) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().print("]");

            if (node.getRepeatExpr() != null) {
                data().getStream().print(" repeat ");
                print(node.getRepeatExpr());
            }
        }

        default void print(AbstractIRNode node) {
            throw new RuntimeException(node.getFromLineNumber() + ": Method print(...) is not overridden for class: " + node.getClass().getCanonicalName() + ". We do not know how to pretty print this.");
        }

        default void print(Port node) {
            data().getStream().print(node.getName());
        }

        default void print(Match node) {
            //print(node.getDeclaration());
            //data().getStream().print(":");
            print(node.getExpression());
        }

        default void print(InputVarDecl node) {
            // This node is rough, not sure if it prints correctly.
            data().getStream().print(node.getName());
            data().spacesInc();
            node.forEachChild(this::print);
            data().spacesDec();
        }

        default void print(LocalVarDecl node) {
            if (node.getType() != null) {
                print(node.getType());
            }
            data().getStream().print(" " + node.getName());
            // Print a value if one is assigned on initialisation.
            if (node.getValue() != null) {
                data().getStream().print(" := ");
                print(node.getValue());
            }
        }

        default void print(ActionGeneratorStmt node) {
            data().getStream().print(data().spaces + "for ");
            print(node.getGenerator());
            boolean firstFilter = true;
            for (Expression expr : node.getFilters()) {
                if (firstFilter) {
                    firstFilter = false;
                } else {
                    data().getStream().print(", ");
                }
                print(expr);
            }
            data().getStream().println(" generate");
            data().spacesInc();

            for (Action action : node.getActions()) {
                print(action);
                data().getStream().println("");
            }

            for (ActionCase actionCase : node.getActionCases()) {
                print(actionCase);
                data().getStream().println("");
            }

            for (ActionGeneratorStmt stmt : node.getActionGeneratorStmts()) {
                print(stmt);
                data().getStream().println("");
            }

            data().spacesDec();
            data().getStream().println(data().spaces + "end");
        }


        default void print(ExprCase node) {
            for (ExprCase.Alternative alternative : node.getAlternatives()) {
                if (((ExprLiteral) alternative.getExpression()).getText() == "True") {
                    print(alternative);
                }
            }
        }

        default void print(ExprCase.Alternative node) {
            print(node.getPattern());
        }

        default void print(PatternBinding node) {
            print(node.getDeclaration());
        }

        default void print(ExprVariable node) {
            print(node.getVariable());
        }

        default void print(Variable node) {
            data().getStream().print(node.getOriginalName());
        }

        default void print(PatternVarDecl node) {
            data().getStream().print(node.getName());
        }

        default void print(StmtAssignment node) {
            data().getStream().print(data().spaces);
            print(node.getLValue());
            data().getStream().print(" := ");
            print(node.getExpression());
            data().getStream().println(";");
        }

        default void print(StmtIf node) {
            data().getStream().print(data().spaces + "if ");
            print(node.getCondition());
            data().getStream().println(" then");
            data().spacesInc();
            for (Statement stmt : node.getThenBranch()) {
                print(stmt);
            }
            //data().getStream().print( node.getName());
            //print(node.getLValue());
            //data().getStream().print(" := ");
            //print(node.getExpression());
            //data().getStream().print(";");
            data().spacesDec();
            data().getStream().println(data().spaces + "end");
        }

        default void print(StmtForeach node) {
            data().getStream().print(data().spaces + "foreach ");
            print(node.getGenerator());
            data().getStream().println(" do");
            data().spacesInc();
            for (Statement stmt : node.getBody()) {
                print(stmt);
            }
            data().spacesDec();
            data().getStream().println(data().spaces + "end");
        }

        default void print(StmtWhile node) {
            data().getStream().print(data().spaces + "while ");
            print(node.getCondition());
            data().getStream().println(" do");
            data().spacesInc();
            for (Statement stmt : node.getBody()) {
                print(stmt);
            }
            data().spacesDec();
            data().getStream().println(data().spaces + "end");
        }

        default void print(LValueVariable node) {
            //data().getStream().print(node.getName());
            print(node.getVariable());
        }

        default void print(LValueIndexer node) {
            print(node.getStructure());
            data().getStream().print("[");
            print(node.getIndex());
            data().getStream().print("]");
        }

        default void print(ExprBinaryOp node) {
            //System.out.println(node.getFromLineNumber() + ": " + node.getFromColumnNumber() + " to " + node
            // .getToLineNumber() + ": " + node.getToColumnNumber()+ " " + node.hasParenthesis());
            if (node.hasParenthesis()) {
                data().getStream().print("(");
            }
            //data().getStream().print("-"+tree().parent(node).getClass()+"-");
            print(node.getOperands().get(0));
            for (int i = 0; i < node.getOperations().size(); i++) {
                String operation = node.getOperations().get(i);
                if (operation.equals("%") && data().generateOrcc) { // For ORCC projects % operation is represented
                    // with "mod"
                    operation = "mod";
                }
                data().getStream().print(" " + operation + " ");
                print(node.getOperands().get(i + 1));
            }
            if (node.hasParenthesis()) {
                data().getStream().print(")");
            }
        }

        default void print(ExprApplication node) {
            print(node.getFunction());
            data().getStream().print("(");
            //print(node.getOperands().get(0));
            for (int i = 0; i < node.getArgs().size(); i++) {
                print(node.getArgs().get(i));
                if (i != node.getArgs().size() - 1) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().print(")");
        }

        default void print(ExprIf node) {
            IRNode entityParent = tree().parent(node);
            // If the parent is an ExprIf, then this is part of an elseif and does not need an if
            if (!(entityParent instanceof ExprIf)) {
                data().getStream().print("if ");
            }
            print(node.getCondition());
            data().getStream().print(" then ");
            print(node.getThenExpr());
            if (node.getElseExpr() instanceof ExprIf) {
                data().getStream().print(" elsif ");
            } else {
                data().getStream().print(" else ");
            }
            print(node.getElseExpr());
            // If the parent is an ExprIf, then this is part of an elseif and does not a closing end
            if (!(entityParent instanceof ExprIf)) {
                data().getStream().print(" end");
            }
        }

        default void print(StmtCall node) {
            data().getStream().print(data().spaces);
            print(node.getProcedure());
            data().getStream().print("(");
            //print(node.getOperands().get(0));
            for (int i = 0; i < node.getArgs().size(); i++) {
                print(node.getArgs().get(i));
                if (i != node.getArgs().size() - 1) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().println(");");
        }

        default void print(ExprUnaryOp node) {
            //System.out.println(node.getFromLineNumber() + ": " + node.getFromColumnNumber() + " to " + node
            // .getToLineNumber() + ": " + node.getToColumnNumber()+ " " + node.hasParenthesis());
            if (node.hasParenthesis()) {
                data().getStream().print("(");
            }
            //print(node.getOperands().get(0));
            //for(int i = 0; i < node.getOperations().size(); i++){
            //	data().getStream().print(" " + node.getOperations().get(i) + " ");
            //	print(node.getOperands().get(i+1));
            //}
            data().getStream().print(node.getOperation());
            print(node.getOperand());

            if (node.hasParenthesis()) {
                data().getStream().print(")");
            }
        }

        default void print(ExprList node) {
            if (!data().noBraces)
                data().getStream().print("[");
            for (int i = 0; i < node.getElements().size(); i++) {
                print(node.getElements().get(i));
                if (i != node.getElements().size() - 1) {
                    data().getStream().print(", ");
                }
            }
            if (!data().noBraces)
                data().getStream().print("]");
        }

        default void print(ExprIndexer node) {
            print(node.getStructure());
            data().getStream().print("[");
            print(node.getIndex());
            data().getStream().print("]");
        }

        default void print(ExprTuple node) {
            //System.out.println(node.getFromLineNumber() + ": " + node.getFromColumnNumber() + " to " + node
            // .getToLineNumber() + ": " + node.getToColumnNumber()+ " " + node.hasParenthesis());
            if (node.hasParenthesis()) {
                data().getStream().print("(");
            }

            Expression lastExpr = node.getElements().get(node.getElements().size() - 1);
            for (Expression expr : node.getElements()) {
                print(expr);
                if (expr != lastExpr) {
                    data().getStream().println(",");
                    data().getStream().print(data().spaces);
                }
            }

            if (node.hasParenthesis()) {
                data().getStream().print(")");
            }
        }

        default void print(ScheduleFSM node) {
            data().getStream().print(data().spaces + "schedule fsm " + node.getInitialState() + ":");
            data().getStream().println();
            data().spacesInc();
            for (Transition transition : node.getTransitions()) {
                data().getStream().print(data().spaces);
                print(transition);
                data().getStream().println(";");
            }
            data().spacesDec();
            data().getStream().println(data().spaces + "end");
        }

        default void print(Transition node) {
            data().getStream().print(node.getSourceState() + "(");
            for (int i = 0; i < node.getActionTags().size(); i++) {
                print(node.getActionTags().get(i));
                if (i != node.getActionTags().size() - 1) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().print(") --> " + node.getDestinationState());
        }

        default void print(QID node) {
            data().getStream().print(node.getLast());
        }

        default void print(GlobalEntityDecl node) {
            node.forEachChild(this::print);
        }

        default void print(GlobalVarDecl node) {
            printVarDecl(node, data());
        }

        default void printVarDecl(VarDecl node, PrettyPrinterData data) {
            // These are rather annoying to print, as they can be either a Function,
            // Procedure or Variable declaration without a simple way to differentiate.
            // We need to check the expressions stored in node.getValue() to differentiate
            // ... and hope that this is a valid way of checking
            if (node.getValue() instanceof ExprLambda) { // Corresponds to a Function decl
                data().getStream().print(data().spaces + "function " + node.getName());
                print(node.getValue());
                data().getStream().println();
            } else if (node.getValue() instanceof ExprProc) {
                data().getStream().print(data().spaces + "procedure " + node.getName());
                print(node.getValue());
                data().getStream().println();
            } else { // Corresponds to a Variable decl
                data().getStream().print(data().spaces);
                if (node.getType() != null) {
                    print(node.getType());
                    data().getStream().print(" ");
                }

                // Variables constructed by the compiler have a $ to be unique, this does not compile
                // so we replace it with __ instead so that it can compile.
                String name = node.getName();
                if (name.indexOf("$") == 0) {
                    name = "__" + name.substring(1);
                }
                data().getStream().print(name);
                // Print a value if one is assigned on initialisation.
                if (node.getValue() != null) {
                    data().getStream().print(" := ");
                    print(node.getValue());
                }
                data().getStream().println(";");
            }
        }

        default void print(ExprLambda node) {
            data().getStream().print("(");
            for (int i = 0; i < node.getValueParameters().size(); i++) {
                print(node.getValueParameters().get(i));
                if (i != node.getValueParameters().size() - 1) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().print(") --> ");
            print(node.getReturnType());
            data().getStream().println(":");
            data().spacesInc();
            data().getStream().print(data().spaces);
            print(node.getBody());
            data().spacesDec();
            data().getStream().println();
            data().getStream().println(data().spaces + "end");
        }

        default void print(ExprProc node) {
            data().getStream().print("(");
            for (int i = 0; i < node.getValueParameters().size(); i++) {
                print(node.getValueParameters().get(i));
                if (i != node.getValueParameters().size() - 1) {
                    data().getStream().print(", ");
                }
            }
            data().getStream().println(")");

            // I expect this to only be a StmtBlock with both the "var" and "begin" keywords defined within
            for (Statement stmt : node.getBody()) {
                print(stmt);
            }
            // The end is part of the StmtBlock
        }

        default void print(StmtBlock node) {
            data().getStream().println(data().spaces + "var");
            data().spacesInc();
            for (int i = 0; i < node.getVarDecls().size(); i++) {
                VarDecl varDecl = node.getVarDecls().get(i);
                data().getStream().print(data().spaces);
                print(varDecl);
                if (i != node.getVarDecls().size() - 1) {
                    data().getStream().print(",");
                }
                data().getStream().println();
            }
            data().spacesDec();

            data().getStream().println(data().spaces + "begin");
            data().spacesInc();
            for (Statement stmt : node.getStatements()) {
                print(stmt);
            }
            data().spacesDec();
            data().getStream().println(data().spaces + "end");
        }

        default void print(SingleImport node) {
            data().getStream().print(data().spaces + "import ");
            if (node.getKind().getDescription().equals("variable")) {
                if (!data().generateOrcc) { // The var is not added when generating imports for ORCC
                    data().getStream().print("var ");
                }
            } else {
                throw new RuntimeException("We have not implemented PrettyPrint for a Import.Kind = " + node.getKind().getDescription());
            }
            data().getStream().print(node.getGlobalName().toString());
            if (!node.getLocalName().equals(node.getGlobalName().getLast().toString())) {
                data().getStream().print(" = " + node.getLocalName());
            }
            //System.out.println(node.getLocalName() + " " + node.getGlobalName().getLast() + " " + node.getLocalName
            // ().indexOf(node.getGlobalName().getLast().toString()));
            data().getStream().println(";");
        }

        default void print(Import.Kind node) {

        }

    }
}

// A class that is passed around the pretty print Module/aspect, allowing
// for persistent storage while traversing the AST.
class PrettyPrinterData {
    // Stores the number of spaces, used for keeping track of indenting
    public String spaces;
    public String entityName = "";
    // Directoy to write files to if enabled
    public String directory = null;
    // This is hack for the ExprList construct
    // For some reason the ExprComprehension.getCollection() object evaluates to an ExprList.
    // So pretty printing:
    // List(type:float, size=n_UEs * COMPLEX) matrixHRow := [matrixH[antIndexOut*n_UEs*COMPLEX+i] : for uint i in 0 .
    // . n_UEs * COMPLEX - 1]
    // Ends up giving us:
    // List(type: float, size = n_UEs * COMPLEX) matrixHRow := [[matrixH[antIndexOut * n_UEs * COMPLEX + i]]: for
    // uint i in 0 .. n_UEs * COMPLEX - 1]
    // this flag is set used by ExprComprehension and read by ExprList to suppress the extra set of braces
    // There is likely an explanation for this, but I have not spent time analysing it
    public boolean noBraces = false;

    // Indicates that the networks should be printed out using the XML required by ORCC/MPEG standard instead of NL used
    // by StreamBlocks. Also indicates that subtle differences between ORCC and StreamBlocks syntax should be
    // accounted for
    public boolean generateOrcc;
    // If true, prints the elaborated network, if false, prints the original network as it appears in the CAL program.
    public boolean printPostElaboration = false;

    // GlobalVarDecls for a specific file that need to be moved to localVarDecls when printing in ORCC format.
    ImmutableList<GlobalVarDecl> globalVarDecls;

    // Print stream to send data to be printed out on.
    private PrintStream ps;

    public PrettyPrinterData(PrintStream ps) {
        this.spaces = "";
        this.ps = ps;
    }

    public PrintStream getStream() {
        return ps;
    }

    public void setStream(PrintStream ps) {
        this.ps = ps;
    }

    // Add or remove a "tab" (4 spaces)
    public void spacesInc() {
        spaces = spaces + "    ";
    }

    public void spacesDec() {
        spaces = spaces.substring(0, spaces.length() - 4);
    }
}
