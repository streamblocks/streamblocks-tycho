package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.PathSetting;
import se.lth.cs.tycho.settings.Setting;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class PrintNetworkPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Collections.singletonList(printNetwork);
	}

	private static final PathSetting printNetwork = new PathSetting() {
		@Override
		public String getKey() {
			return "print-network";
		}

		@Override
		public String getDescription() {
			return "Prints the network to a file";
		}

		@Override
		public Path defaultValue(Configuration configuration) {
			return null;
		}
	};

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Path file = context.getConfiguration().get(printNetwork);
		if (file != null) {
			try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
				writer.println("Input ports:");
				for (PortDecl inputPort : task.getNetwork().getInputPorts()) {
					writer.println("\t"+inputPort.getName());
				}
				writer.println();
				writer.println("Output ports:");
				for (PortDecl outputPort : task.getNetwork().getOutputPorts()) {
					writer.println("\t"+outputPort.getName());
				}
				writer.println();
				writer.println("Instances:");
				for (Instance instance : task.getNetwork().getInstances()) {
					writer.println(instance.getInstanceName() + " = " + instance.getEntityName() + "(...)");
				}
				writer.println();
				writer.println("Connections:");
				for (Connection connection : task.getNetwork().getConnections()) {
					writer.println(connection);
				}
			} catch (IOException e) {
				context.getReporter().report(new Diagnostic(Diagnostic.Kind.WARNING, "Error while writing to file: "+e.getMessage()));
			}
		}
		return task;
	}
}
