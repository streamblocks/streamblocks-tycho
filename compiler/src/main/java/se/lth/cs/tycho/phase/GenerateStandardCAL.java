package se.lth.cs.tycho.phase;

import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.settings.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class GenerateStandardCAL implements Phase {
    public static Setting<Boolean> printStandardCAL = new OnOffSetting() {
        @Override
        public String getKey() {
            return "print-standard-cal";
        }

        @Override
        public String getDescription() {
            return "Prints the source code after template substitution has been performed, action generators have been unrolled, and the network and arrays of ports have been enumerated.";
        }

        @Override
        public Boolean defaultValue(Configuration configuration) {
            return false;
        }
    };

    public static Setting<Boolean> printStandardCALOrcc = new OnOffSetting() {
        @Override
        public String getKey() {
            return "print-standard-cal-orcc";
        }

        @Override
        public String getDescription() {
            return "Performs same function as print-standard-cal flag but ensures that the CAL code printed is suitable for the ORCC compiler with the FNL network description. Takes precedence over print-standard-cal.";
        }

        @Override
        public Boolean defaultValue(Configuration configuration) {
            return false;
        }
    };

    public static Setting<Boolean> printStandardCALDirectory = new OnOffSetting() {
        @Override
        public String getKey() {
            return "print-to-directory";
        }

        @Override
        public String getDescription() {
            return "If this flag is set and either the print-standard-cal-orcc or print-standard-cal flags are set, then the generated CAL code will get written to files in the \"generated\" directory.";
        }

        @Override
        public Boolean defaultValue(Configuration configuration) {
            return false;
        }
    };

    @Override
    public List<Setting<?>> getPhaseSettings() {
        return Arrays.asList(printStandardCAL, printStandardCALOrcc, printStandardCALDirectory);
    }

    @Override
    public String getDescription() {
        return "Pretty prints standard CAL source code after template substitution has been performed, action " +
                "generators have been unrolled, and the network and arrays of ports have been enumerated. Stops " +
                "compilation after printing. Enabled with --set print-standard-cal=on flag. This phase can print " +
                "code in backward compatibility mode for ORCC with the flag --set print-standard-cal-orcc=on. This " +
                "phase can also print files to a \"generated\" directory with --set print-to-directory=on. ";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) {
        // 1. Check that the pretty-print flag is set and if it is, perform pretty print
        if (context.getConfiguration().get(printStandardCAL) || context.getConfiguration().get(printStandardCALOrcc)) {

            // 2. We create a print stream object for printing the pretty print files to
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os);

            // 3. Create a data object to be passed around the tree. This allows
            // for some persistent storage between different calls of print.
            PrettyPrinterData data = new PrettyPrinterData(ps);
            data.printPostElaboration = true;
            if(context.getConfiguration().get(printStandardCALOrcc)){
                data.generateOrcc = true;
            }else{
                data.generateOrcc = false;
            }

            // 4. Get the directory to print CAL files to if it is specified
            if(context.getConfiguration().get(printStandardCALDirectory)){
                data.directory = "generated";
            }

            // 5. Create the printer object and bind the data object to it.
            PrettyPrint.PrettyPrinter printer = MultiJ.from(PrettyPrint.PrettyPrinter.class)
                    .bind("data").to(data)
                    .bind("tree").to(task.getModule(TreeShadow.key))
                    .instance();

            // 6. Call print on the top level IRNode - which is the CompilationTask
            try {
                printer.print(task);

                // 6.1 Deal with the print stream
                System.out.println(os.toString("UTF8"));
            } catch (Exception e) {
                context.getReporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Error in PrettyPrintPostTemplateSubstitution Phase: " + e.toString()));
                e.printStackTrace();
            }

            // 7. Inject an error to force an exit
            System.out.println("Pretty Printing code instead of compiling. No further compilation stages will be evaluated.");
            System.exit(0);
        }
        // 8. A phase must always return a compilation task, in this case we
        // return the received task forward as we made no modifications.
        return task;
    }
}