package se.lth.cs.tycho.ir.network;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class Network extends AbstractIRNode {
	private final ImmutableList<PortDecl> inputPorts;
	private final ImmutableList<PortDecl> outputPorts;
	private final ImmutableList<Instance> instances;
	private final ImmutableList<Connection> connections;

	public Network(List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<Instance> instances, List<Connection> connections) {
		this(null, inputPorts, outputPorts, instances, connections);
	}

	private Network(Network original, List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<Instance> instances, List<Connection> connections) {
		super(original);
		this.inputPorts = ImmutableList.from(inputPorts);
		this.outputPorts = ImmutableList.from(outputPorts);
		this.instances = ImmutableList.from(instances);
		this.connections = ImmutableList.from(connections);
	}

	public Network copy(List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<Instance> instances, List<Connection> connections) {
		if (Lists.sameElements(this.inputPorts, inputPorts)
				&& Lists.sameElements(this.outputPorts, outputPorts)
				&& Lists.sameElements(this.instances, instances)
				&& Lists.sameElements(this.connections, connections)) {
			return this;
		} else {
			return new Network(this, inputPorts, outputPorts, instances, connections);
		}
	}
	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public Network withInputPorts(List<PortDecl> inputPorts) {
		return copy(inputPorts, outputPorts, instances, connections);
	}

	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}

	public Network withOutputPorts(List<PortDecl> outputPorts) {
		return copy(inputPorts, outputPorts, instances, connections);
	}

	public ImmutableList<Instance> getInstances() {
		return instances;
	}

	public Network withInstances(List<Instance> instances) {
		return copy(inputPorts, outputPorts, instances, connections);
	}

	public ImmutableList<Connection> getConnections() {
		return connections;
	}

	public Network withConnections(List<Connection> connections) {
		return copy(inputPorts, outputPorts, instances, connections);
	}

	@Override
	public Network transformChildren(Transformation transformation) {
		return copy(
				transformation.mapChecked(PortDecl.class, inputPorts),
				transformation.mapChecked(PortDecl.class, outputPorts),
				transformation.mapChecked(Instance.class, instances),
				transformation.mapChecked(Connection.class, connections));
	}

	@Override
	public Network clone() {
		return (Network) super.clone();
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		inputPorts.forEach(action);
		outputPorts.forEach(action);
		instances.forEach(action);
		connections.forEach(action);
	}

	@Override
	public Network deepClone() {
		return (Network) super.deepClone();
	}
}
