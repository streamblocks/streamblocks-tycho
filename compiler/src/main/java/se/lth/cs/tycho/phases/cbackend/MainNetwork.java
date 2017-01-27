package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.phases.attributes.GlobalNames;
import se.lth.cs.tycho.phases.attributes.Names;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module
public interface MainNetwork {
	@Binding
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default Names names() {
		return backend().names();
	}

	default GlobalNames globalNames() {
		return backend().globalNames();
	}

	default Code code() {
		return backend().code();
	}

	default void main(Network network, String name) {
		List<Connection> connections = network.getConnections();
		List<Instance> instances = network.getInstances();


		emitter().emit("static void run(int argc, char **argv) {");
		emitter().increaseIndentation();

		emitter().emit("init_global_variables();");

		int nbrOfPorts = network.getInputPorts().size() + network.getOutputPorts().size();
		emitter().emit("if (argc != %d) {", nbrOfPorts+1);
		emitter().increaseIndentation();
		emitter().emit("fprintf(stderr, \"Wrong number of arguments. Expected %d but was %%d\\n\", argc-1);", nbrOfPorts);
		String args = Stream.concat(network.getInputPorts().stream(), network.getOutputPorts().stream())
				.map(PortDecl::getName)
				.collect(Collectors.joining("> <", "<", ">"));
		emitter().emit("fprintf(stderr, \"Usage: %%s %s\\n\", argv[0]);", args);
		emitter().emit("return;");
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");


		emitter().emit("fifo_reader *fifo[%d];", connections.size());
		{
			int i = 0;
			for (Connection conn : connections) {
				String type;
				if (conn.getSource().getInstance().isPresent()) {
					Instance instance = network.getInstances().stream()
							.filter(inst -> inst.getInstanceName().equals(conn.getSource().getInstance().get()))
							.findFirst().get();
					EntityDecl entity = globalNames().entityDecl(QID.of(instance.getEntityName()), true);
					PortDecl portDecl = entity.getEntity().getOutputPorts().stream()
							.filter(port -> port.getName().equals(conn.getSource().getPort()))
							.findFirst().orElseThrow(() -> new AssertionError("Missing source port: " + conn));

					type = code().type(backend().types().declaredPortType(portDecl));
				} else {
					PortDecl portDecl = network.getInputPorts().stream()
							.filter(port -> port.getName().equals(conn.getSource().getPort()))
							.findFirst().get();
					type = code().type(backend().types().declaredPortType(portDecl));
				}
				emitter().emit("fifo[%d] = fifo_create(%d);", i, i);
				i = i + 1;
			}
		}
		emitter().emit("");
		for (Instance instance : instances) {
			if(!name.equals(instance.getEntityName())) {
				continue;
			}
			emitter().emit("%s_state %s;", instance.getEntityName(), instance.getInstanceName());
			List<String> initParameters = new ArrayList<>();
			initParameters.add("&" + instance.getInstanceName());
			EntityDecl entityDecl = globalNames().entityDecl(QID.of(instance.getEntityName()), true);
			for (VarDecl par : entityDecl.getEntity().getValueParameters()) {
				boolean assigned = false;
				for (Parameter<Expression> assignment : instance.getValueParameters()) {
					if (par.getName().equals(assignment.getName())) {
						initParameters.add(code().evaluate(assignment.getValue()));
						assigned = true;
					}
				}
				if (!assigned) {
					throw new RuntimeException(String.format("Could not assign to %s. Candidates: {%s}.", par.getName(), String.join(", ", instance.getValueParameters().map(Parameter::getName))));
				}
			}

			for (PortDecl port : entityDecl.getEntity().getInputPorts()) {
				int i = 0;
				for (Connection conn : connections) {
					if (conn.getTarget().getInstance().isPresent()
							&& conn.getTarget().getInstance().get().equals(instance.getInstanceName())
							&& conn.getTarget().getPort().equals(port.getName())) {
						break;
					}
					i = i + 1;
				}
				initParameters.add(String.format("fifo[%d]", i));
			}
			for (PortDecl port : entityDecl.getEntity().getOutputPorts()) {
				int i = 0;
				BitSet outgoing = new BitSet();
				for (Connection conn : connections) {
					if (conn.getSource().getInstance().isPresent()
							&& conn.getSource().getInstance().get().equals(instance.getInstanceName())
							&& conn.getSource().getPort().equals(port.getName())) {
						outgoing.set(i);
					}
					i = i + 1;
				}
				String channels = outgoing.stream().mapToObj(o -> String.format("fifo[%d]", o)).collect(Collectors.joining(", "));
				emitter().emit("fifo_reader *%s_%s[%d] = { %s };", instance.getInstanceName(), port.getName(), outgoing.cardinality(), channels);
				initParameters.add(String.format("%s_%s", instance.getInstanceName(), port.getName()));
				initParameters.add(Integer.toString(outgoing.cardinality()));
			}
			emitter().emit("%s_init_actor(%s);", instance.getEntityName(), String.join(", ", initParameters));
			emitter().emit("");
		}

		int argi = 1;
		for (PortDecl port : network.getInputPorts()) {
			BitSet outgoing = new BitSet();
			int i = 0;
			for (Connection conn : connections) {
				if (!conn.getSource().getInstance().isPresent() && conn.getSource().getPort().equals(port.getName())) {
					outgoing.set(i);
				}
				i = i + 1;
			}
			String channels = outgoing.stream().mapToObj(o -> String.format("fifo[%d]", o)).collect(Collectors.joining(", "));
			emitter().emit("FILE *%s_input_file = fopen(argv[%d], \"r\");", port.getName(), argi);
			emitter().emit("fifo_reader *%s_fifos[%d] = { %s };", port.getName(), outgoing.cardinality(), channels);
			emitter().emit("input_actor_t *%s_input_actor = input_actor_create(%1$s_input_file, %1$s_fifos, %d);", port.getName(), outgoing.cardinality());
			emitter().emit("");
			argi = argi + 1;
		}
		for (PortDecl port : network.getOutputPorts()) {
			int i = 0;
			for (Connection conn : connections) {
				if (!conn.getTarget().getInstance().isPresent() && conn.getTarget().getPort().equals(port.getName())) {
					emitter().emit("FILE *%s_output_file = fopen(argv[%d], \"w\");", port.getName(), argi);
					emitter().emit("output_actor_t *%s_output_actor = output_actor_create(%1$s_output_file, fifo[%d]);", port.getName(), i);
					emitter().emit("");
					argi = argi + 1;
					break;
				}
				i = i + 1;
			}
		}

		emitter().emit("_Bool progress;");
		emitter().emit("do {");
		emitter().increaseIndentation();
		emitter().emit("progress = false;");
		for (PortDecl inputPort : network.getInputPorts()) {
			emitter().emit("progress |= input_actor_run(%s_input_actor);", inputPort.getName());
		}
		for (Instance instance : instances) {
			if(!name.equals(instance.getEntityName())) {
				continue;
			}

			emitter().emit("progress |= %s_run(&%s);", instance.getEntityName(), instance.getInstanceName());
		}
		for (PortDecl outputPort : network.getOutputPorts()) {
			emitter().emit("progress |= output_actor_run(%s_output_actor);", outputPort.getName());
		}
		emitter().decreaseIndentation();
		emitter().emit("} while (progress && !interrupted);");
		emitter().emit("");

		emitter().emit("for (int i = 0; i < sizeof(fifo)/sizeof(fifo[0]); i++) {");
		emitter().increaseIndentation();
		emitter().emit("fifo_destroy(fifo[i]);");
		emitter().decreaseIndentation();
		emitter().emit("}");


		for (PortDecl port : network.getInputPorts()) {
			emitter().emit("fclose(%s_input_file);", port.getName());
		}

		for (PortDecl port : network.getOutputPorts()) {
			emitter().emit("fclose(%s_output_file);", port.getName());
		}

		for (PortDecl port : network.getInputPorts()) {
			emitter().emit("input_actor_destroy(%s_input_actor);", port.getName());
		}

		for (PortDecl port : network.getOutputPorts()) {
			emitter().emit("output_actor_destroy(%s_output_actor);", port.getName());
		}

		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
		emitter().emit("");
	}

}
