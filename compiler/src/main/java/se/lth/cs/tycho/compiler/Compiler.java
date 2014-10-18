package se.lth.cs.tycho.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import se.lth.cs.tycho.backend.c.Backend;
import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instantiation.Instantiator;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemCalRepository;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.messages.MessageWriter;
import se.lth.cs.tycho.transform.caltoam.ActorStates;
import se.lth.cs.tycho.transform.filter.SelectFirstInstruction;

public class Compiler {
	public static void main(String[] args) throws IOException {
		OptionParser parser = new OptionParser();
		OptionSpec<File> outputDir = parser.accepts("output-dir", "Output directory for the compilaiton")
				.withRequiredArg()
				.ofType(File.class)
				.defaultsTo(new File("."));
		OptionSpec<File> sourceDir = parser.accepts("source-dir", "The directory where the source code is located")
				.withRequiredArg()
				.withValuesSeparatedBy(':')
				.ofType(File.class)
				.defaultsTo(new File("."));

		OptionSpec<String> entity = parser.nonOptions("The qualified identifier of the entity to compile.");

		OptionSet opts = parser.parse(args);

		if (opts.hasArgument(entity) && opts.valuesOf(entity).size() == 1) {
			MessageReporter msg = new MessageWriter();
			DeclarationLoader loader = new DeclarationLoader(msg);
			for (File sourceDirectory : opts.valuesOf(sourceDir)) {
				Path sourcePath = sourceDirectory.toPath();
				loader.addRepository(new FileSystemCalRepository(sourcePath));
				loader.addRepository(new FileSystemXdfRepository(sourcePath));
			}
			Instantiator instantiator = new Instantiator(loader,
					Arrays.asList(SelectFirstInstruction<ActorStates.State>::new));
			QID qid = QID.parse(opts.valueOf(entity));
			Instance inst;
			try {
				inst = instantiator.instantiate(qid, null);
			} catch (AmbiguityException e) {
				throw new RuntimeException(e);
			}
			Path outputFile = opts.valueOf(outputDir).toPath().resolve(qid.getLast().toString() + ".c");
			PrintWriter out = new PrintWriter(Files.newBufferedWriter(outputFile), true);
			Backend.generateCode((Network) inst, out);
		} else {
			System.out.println("Usage: java " + Compiler.class.getCanonicalName() + " [options] entity");
			System.out.println();
			parser.printHelpOn(System.out);
			System.exit(1);
		}

	}
}
