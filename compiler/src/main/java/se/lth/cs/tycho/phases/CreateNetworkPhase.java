package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.GlobalDeclarations;
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
		assert task.getIdentifier().getNameCount() == 1 : "Entity must be in the root namespace.";
		EntityDecl topLevelEntity = GlobalDeclarations.getEntity(task, task.getIdentifier());
		Network network = createNetwork(topLevelEntity);
		return task.withNetwork(network);
	}

	private Network createNetwork(EntityDecl entityDecl) {
		assert entityDecl.getEntity().getTypeParameters().isEmpty();
		assert entityDecl.getEntity().getValueParameters().isEmpty();
		ImmutableList<PortDecl> inputPorts = entityDecl.getEntity().getInputPorts().map(PortDecl::deepClone);
		ImmutableList<PortDecl> outputPorts = entityDecl.getEntity().getOutputPorts().map(PortDecl::deepClone);
		String instance = entityDecl.getName();
		ImmutableList<Instance> instances = ImmutableList.of(new Instance(instance, entityDecl.getName(), ImmutableList.empty(), ImmutableList.empty()));
		ImmutableList<Connection> in = inputPorts.map(port -> new Connection(new Connection.End(Optional.empty(), port.getName()), new Connection.End(Optional.of(instance), port.getName())));
		ImmutableList<Connection> out = outputPorts.map(port -> new Connection(new Connection.End(Optional.of(instance), port.getName()), new Connection.End(Optional.empty(), port.getName())));
		return new Network(inputPorts, outputPorts, instances, ImmutableList.concat(in, out));
	}

	@Override
	public Set<Class<? extends Phase>> dependencies() {
		return Collections.singleton(RemoveNamespacesPhase.class);
	}
}
