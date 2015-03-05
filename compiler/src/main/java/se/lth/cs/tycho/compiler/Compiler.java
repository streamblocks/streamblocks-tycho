package se.lth.cs.tycho.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import se.lth.cs.tycho.backend.c.Backend;
import se.lth.cs.tycho.backend.c.NetworkFunctions;
import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.instantiation.Instantiator;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemCalRepository;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.messages.MessageWriter;
import se.lth.cs.tycho.transform.caltoam.CalActorStates;
import se.lth.cs.tycho.transform.net.NetworkUtils;
import se.lth.cs.tycho.transform.outcond.OutputConditionAdder;
import se.lth.cs.tycho.transform.outcond.OutputConditionState;
import se.lth.cs.tycho.transform.reduction.ConditionProbabilityReducer;
import se.lth.cs.tycho.transform.reduction.ControllerWrapper;
import se.lth.cs.tycho.transform.reduction.MaxDiffOfReachableActions;
import se.lth.cs.tycho.transform.reduction.MinDiffOfReachableActions;
import se.lth.cs.tycho.transform.reduction.NearestExecReducer;
import se.lth.cs.tycho.transform.reduction.SelectFirstReducer;
import se.lth.cs.tycho.transform.reduction.SelectMaximum;
import se.lth.cs.tycho.transform.reduction.SelectRandomReducer;
import se.lth.cs.tycho.util.ControllerToGraphviz;

public class Compiler {
	public static void main(String[] args) throws IOException, InterruptedException {

		// Command line parsing

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
		OptionSpec<String> reducers = parser.accepts("reducers", "Actor machine reducers.")
				.withRequiredArg()
				.withValuesSeparatedBy(":")
				.defaultsTo("select-first");
		OptionSpec<Void> printActorNames = parser.accepts("print-instance-names",
				"Prints the names of the nodes of the flattened network.");
		OptionSpec<File> printControllers = parser.accepts("print-controller-graphs",
				"Writes a graphviz file for each controller")
				.withOptionalArg()
				.ofType(File.class)
				.defaultsTo(new File("."));

		OptionSpec<String> entity = parser.nonOptions("The qualified identifier of the entity to compile.");

		OptionSet opts = parser.parse(args);

		if (opts.hasArgument(entity) && opts.valuesOf(entity).size() == 1) {

			// Entity loading

			MessageReporter msg = new MessageWriter();
			DeclarationLoader loader = new DeclarationLoader(msg);
			for (File sourceDirectory : opts.valuesOf(sourceDir)) {
				Path sourcePath = sourceDirectory.toPath();
				loader.addRepository(new FileSystemCalRepository(sourcePath));
				loader.addRepository(new FileSystemXdfRepository(sourcePath));
			}

			// Controller transformation building

			List<ControllerWrapper<CalActorStates.State, CalActorStates.State>> wrappers = new ArrayList<>();
			for (String reducer : opts.valuesOf(reducers)) {
				if (reducer.equals("select-first")) {
					wrappers.add(SelectFirstReducer.wrapper());
				} else if (reducer.startsWith("select-random(") && reducer.endsWith(")")) {
					String param = reducer.substring("select-random(".length(), reducer.length() - 1);
					if (param.isEmpty()) {
						wrappers.add(SelectRandomReducer.wrapper(new Random()));
					} else {
						try {
							long seed = Long.parseLong(param);
							wrappers.add(SelectRandomReducer.wrapper(new Random(seed)));
						} catch (NumberFormatException e) {
							msg.report(Message.warning("Illegal argument to reducer " + reducer
									+ ". Ignoring this reducer."));
						}
					}
				} else if (reducer.startsWith("prioritize-probable-conditions(") && reducer.endsWith(")")) {
					String param = reducer.substring("prioritize-probable-conditions(".length(), reducer.length()-1);
					Path path = Paths.get(param);
					wrappers.add(ConditionProbabilityReducer.wrapper(path, 0.1, msg));
				} else if (reducer.startsWith("shortest-path-to-most-probable-action(") && reducer.endsWith(")")) {
					String param = reducer.substring("shortest-path-to-most-probable-action(".length(), reducer.length()-1);
					Path path = Paths.get(param);
					wrappers.add(NearestExecReducer.wrapper(path, msg));
				} else if (reducer.equals("prioritize-call")) {
					wrappers.add(SelectMaximum.selectCall());
				} else if (reducer.equals("prioritize-test")) {
					wrappers.add(SelectMaximum.selectTest());
				} else if (reducer.equals("prioritize-wait")) {
					wrappers.add(SelectMaximum.selectWait());
				} else if (reducer.equals("prioritize-input-test")) {
					wrappers.add(SelectMaximum.selectInputTest());
				} else if (reducer.equals("prioritize-guard-test")) {
					wrappers.add(SelectMaximum.selectPredicateTest());
				} else if (reducer.equals("prioritize-input-test-for-many-tokens")) {
					wrappers.add(SelectMaximum.selectTestOfManyTokens());
				} else if (reducer.equals("prioritize-input-test-for-few-tokens")) {
					wrappers.add(SelectMaximum.selectTestOfFewTokens());
				} else if (reducer.equals("prioritize-diverging-tests")) {
					wrappers.add(MaxDiffOfReachableActions.wrapper());
				} else if (reducer.equals("prioritize-non-diverging-tests")) {
					wrappers.add(MinDiffOfReachableActions.wrapper());
				} else {
					msg.report(Message.warning("Ignoring unknown reducer \"" + reducer + "\"."));
				}
			}

			// Instantiation of network and actor machines

			Instantiator instantiator = new Instantiator(loader, wrappers);
			QID qid = QID.parse(opts.valueOf(entity));
			Network net;
			try {
				Instance inst = instantiator.instantiate(qid, null, QID.empty());
				if (inst instanceof ActorMachine) {
					net = NetworkFunctions.fromSingleNode(inst, qid.getLast().toString());
				} else if (inst instanceof Network) {
					net = (Network) inst;
				} else {
					net = null;
				}
			} catch (AmbiguityException e) {
				throw new RuntimeException(e);
			}
			
			// Output conditions
			
			net = NetworkUtils.transformNodes(net, (Node node) -> {
				Instance inst = node.getContent();
				if (inst instanceof ActorMachine) {
					ActorMachine actorMachine = (ActorMachine) inst;
					return node.copy(
							node.getName(),
							OutputConditionAdder.addOutputConditions(
									actorMachine,
									QID.of(node.getName()),
									Collections.singletonList(SelectFirstReducer.<OutputConditionState> wrapper())),
							node.getToolAttributes());
				} else {
					return node;
				}
			});

			// Instance name printing

			if (opts.has(printActorNames)) {
				int i = 0;
				for (Node n : net.getNodes()) {
					msg.report(Message.note(String.format("Instance %d: %s", i++, n.getName())));
				}
			}
			
			// Graphviz printing
			
			if (opts.has(printControllers)) {
				for (Node n : net.getNodes()) {
					File f = opts.valueOf(printControllers).toPath().resolve(n.getName() + ".gv").toFile();
					ControllerToGraphviz.print(new PrintWriter(f), (ActorMachine) n.getContent(), n.getName());
				}
			}

			// Code generation

			Path outputFile = opts.valueOf(outputDir).toPath().resolve(qid.getLast().toString() + ".c");
			PrintWriter out = new PrintWriter(Files.newBufferedWriter(outputFile), true);
			Backend.generateCode(loader, net, out);
			out.close();

		} else {
			System.out.println("Usage: java " + Compiler.class.getCanonicalName() + " [options] entity");
			System.out.println();
			parser.printHelpOn(System.out);
			System.exit(1);
		}

	}
}
