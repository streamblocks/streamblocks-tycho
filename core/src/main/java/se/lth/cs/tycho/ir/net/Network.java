package se.lth.cs.tycho.ir.net;

import java.util.Objects;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;


/**
 * A Network is a directed graph structure, where {@link Connection}s create links between {@link PortDecl}s. Each {@link se.lth.cs.tycho.ir.Port} is 
 * part of a {@link PortContainer} --- such a  can be either the Network itself, or any of the {@link Node}s inside it.
 * 
 * {@link Network}s can be hierarchical, i.e. sub-networks are represented as {@link Network}s.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Network extends AbstractIRNode implements PortContainer{

	
	public Node getNode(Identifier id) {
		for(Node node : nodes){
			if(node.getIdentifier()==id){
				return node;
			}
		}
		return null;
	}

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
	
	protected Network (IRNode original, ImmutableList<Node> nodes, ImmutableList<Connection> connections, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts) {
		super(original);
		this.nodes = nodes;
		this.connections = connections;
        this.inputPorts = inputPorts;
        this.outputPorts = outputPorts;
	}
	
	public Network copy(ImmutableList<Node> nodes, ImmutableList<Connection> connections, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts){
		if(Objects.equals(this.nodes, nodes) && Lists.equals(this.connections, connections) && Lists.equals(this.inputPorts,  inputPorts) && Lists.equals(this.outputPorts,  outputPorts)){
			return this;
		}
		return new Network(this, nodes, connections, inputPorts, outputPorts);
	}

	private ImmutableList<Node>			nodes;
	private ImmutableList<Connection>	connections;
	private ImmutableList<PortDecl>		inputPorts;
	private ImmutableList<PortDecl>		outputPorts;
}
