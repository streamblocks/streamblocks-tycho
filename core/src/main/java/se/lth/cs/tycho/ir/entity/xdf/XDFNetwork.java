package se.lth.cs.tycho.ir.entity.xdf;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;
import java.util.function.Function;

public class XDFNetwork extends Entity {

	private final ImmutableList<XDFInstance> instances;
	private final ImmutableList<XDFConnection> connections;

	public XDFNetwork(ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts, ImmutableList<XDFInstance> instances, ImmutableList<XDFConnection> connections) {
		this(null, inputPorts, outputPorts, instances, connections);
	}

	public XDFNetwork(IRNode original, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
			ImmutableList<XDFInstance> instances, ImmutableList<XDFConnection> connections) {
		super(original, inputPorts, outputPorts, ImmutableList.empty(), ImmutableList.empty());
		this.instances = instances;
		this.connections = connections;
	}

	public XDFNetwork copy(ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts,
						   ImmutableList<XDFInstance> instances, ImmutableList<XDFConnection> connections) {
		if (Lists.equals(getInputPorts(), inputPorts) && Lists.equals(getOutputPorts(), outputPorts)
				&& Lists.equals(this.instances, instances) && Lists.equals(this.connections, connections)) {
			return this;
		} else {
			return new XDFNetwork(this, inputPorts, outputPorts, instances, connections);
		}
	}

	public ImmutableList<XDFInstance> getInstances() {
		return instances;
	}

	public ImmutableList<XDFConnection> getConnections() {
		return connections;
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitXDFNetwork(this, param);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		super.forEachChild(action);
		instances.forEach(action);
		connections.forEach(action);
	}

	@Override
	public XDFNetwork transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return copy(
				(ImmutableList) getInputPorts().map(transformation),
				(ImmutableList) getOutputPorts().map(transformation),
				(ImmutableList) instances.map(transformation),
				(ImmutableList) connections.map(transformation)
		);
	}
}
