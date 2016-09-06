package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.GlobalDeclarations;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

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
		EntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
		assert entityDecl.getEntity().getTypeParameters().isEmpty();
		assert entityDecl.getEntity().getValueParameters().isEmpty();
		ImmutableList<PortDecl> inputPorts = entityDecl.getEntity().getInputPorts().map(PortDecl::deepClone);
		ImmutableList<PortDecl> outputPorts = entityDecl.getEntity().getOutputPorts().map(PortDecl::deepClone);
		String instance = entityDecl.getName();
		ImmutableList<Instance> instances = ImmutableList.of(new Instance(instance, task.getIdentifier(), ImmutableList.empty(), ImmutableList.empty()));
		ImmutableList<Connection> in = inputPorts.map(port -> new Connection(new Connection.End(Optional.empty(), port.getName()), new Connection.End(Optional.of(instance), port.getName())));
		ImmutableList<Connection> out = outputPorts.map(port -> new Connection(new Connection.End(Optional.of(instance), port.getName()), new Connection.End(Optional.empty(), port.getName())));
		return new Network(inputPorts, outputPorts, instances, ImmutableList.concat(in, out));
	}

}
