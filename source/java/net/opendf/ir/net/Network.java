package net.opendf.ir.net;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;


/**
 * A Network is a directed graph structure, where {@link Connection}s create links between {@link PortDecl}s. Each Port is 
 * part of a {@link DeclEntity} --- such a  can be either the Network itself, or any of the {@link Node}s inside it.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Network extends AbstractIRNode{

	
	public ImmutableList<Node> getNodes() {
		return nodes;
	}
	
	public ImmutableList<Connection> getConnections() {
		return connections;
	}
	
	public ImmutableList<PortDecl> getInputPorts() { return inputPorts; }
	
	public ImmutableList<PortDecl> getOutputPorts() { return outputPorts; }

	//
	// Ctor
	// 
	public Network (ImmutableList<Node> nodes, ImmutableList<Connection> connections, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts) {
		this(null, nodes, connections, inputPorts, outputPorts);
	}
	
	protected Network (Network original, ImmutableList<Node> nodes, ImmutableList<Connection> connections, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts) {
		super(original);
		this.nodes = nodes;
		this.connections = connections;
        this.inputPorts = inputPorts;
        this.outputPorts = outputPorts;
	}
	
	public Network copy(Network original, ImmutableList<Node> nodes, ImmutableList<Connection> connections, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts){
		if(Objects.equals(this.nodes, nodes) && Lists.equals(this.connections, connections) && Lists.equals(this.inputPorts,  inputPorts) && Lists.equals(this.outputPorts,  outputPorts)){
			return original;
		}
		return new Network(this, nodes, connections, inputPorts, outputPorts);
	}

	private ImmutableList<Node>			nodes;
	private ImmutableList<Connection>	connections;
	private ImmutableList<PortDecl>		inputPorts;
	private ImmutableList<PortDecl>		outputPorts;
}
