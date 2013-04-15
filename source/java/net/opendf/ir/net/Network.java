package net.opendf.ir.net;

import java.util.ArrayList;
import java.util.List;

import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.PortDecl;


/**
 * A Network is a directed graph structure, where {@link Connection}s create links between {@link PortDecl}s. Each Port is 
 * part of a {@link DeclEntity} --- such a  can be either the Network itself, or any of the {@link Node}s inside it.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Network {

	
	public List<Node> getNodes() {
		return nodes;
	}
	
	public void addNode(Node n) {
		getNodes().add(n);
	}
	
	public List<Connection> getConnections() {
		return connections;
	}
	
	public void addConnection(Connection c) {
		getConnections().add(c);
	}
	
	public List<PortDecl> getInputPorts() { return inputPorts; }
	
	public List<PortDecl> getOutputPorts() { return outputPorts; }

	//
	// Ctor
	// 
		
	public Network (List<PortDecl> inputPorts, List<PortDecl> outputPorts) {
        this.inputPorts = inputPorts;
        this.outputPorts = outputPorts;
	}

	private List<Node>			nodes = new ArrayList<Node>();
	private List<Connection>	connections = new ArrayList<Connection>();
	private List<PortDecl>		inputPorts;
	private List<PortDecl>		outputPorts;
}
