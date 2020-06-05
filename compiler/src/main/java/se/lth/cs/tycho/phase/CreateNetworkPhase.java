package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Optional;

public class CreateNetworkPhase implements Phase {
	@Override
	public String getDescription() {
		return "Instantiates a network";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Network network = createNetwork(task);
		return task.withNetwork(network);
	}

	private Network createNetwork(CompilationTask task) {
		GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
		assert entityDecl.getEntity().getTypeParameters().isEmpty();
		assert entityDecl.getEntity().getValueParameters().isEmpty();
		ImmutableList<Annotation> annotations = entityDecl.getEntity().getAnnotations().map(Annotation::deepClone);
		ImmutableList<PortDecl> inputPorts = entityDecl.getEntity().getInputPorts().map(PortDecl::deepClone);
		ImmutableList<PortDecl> outputPorts = entityDecl.getEntity().getOutputPorts().map(PortDecl::deepClone);
		String instance = entityDecl.getName();
		ImmutableList<Instance> instances = ImmutableList.of(new Instance(instance, task.getIdentifier(), ImmutableList.empty(), ImmutableList.empty()));
		ImmutableList<Connection> in = inputPorts.map(port -> new Connection(new Connection.End(Optional.empty(), port.getName()), new Connection.End(Optional.of(instance), port.getName())));
		ImmutableList<Connection> out = outputPorts.map(port -> new Connection(new Connection.End(Optional.of(instance), port.getName()), new Connection.End(Optional.empty(), port.getName())));
		return new Network(annotations, inputPorts, outputPorts, instances, ImmutableList.concat(in, out));
	}

}
