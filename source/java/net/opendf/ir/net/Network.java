package net.opendf.ir.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.CompositePortDecl;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.common.ParDecl;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;


/**
 * A Network is a directed graph structure, where {@link Connection}s create links between {@link PortDecl}s. Each Port is 
 * part of a {@link DeclEntity} --- such a  can be either the Network itself, or any of the {@link Node}s inside it.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class Network extends DeclEntity {

	
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
	
	//
	// Ctor
	// 
	
	public Network(String name, NamespaceDecl ns) {
		this (name, ns, new ParDecl [0], new DeclType [0], new DeclVar [0], new CompositePortDecl(null, null), new CompositePortDecl(null, null));
	}
	
	public Network(String name, NamespaceDecl ns, CompositePortDecl inputPorts, CompositePortDecl outputPorts) {
		this (name, ns, new ParDecl [0], new DeclType [0], new DeclVar [0], inputPorts, outputPorts);
	}

	public Network (String name, NamespaceDecl ns, ParDecl [] pars, DeclType [] typeDecls, DeclVar [] varDecls, 
			        CompositePortDecl inputPorts, CompositePortDecl outputPorts) {
		super (name, ns, pars, typeDecls, varDecls, inputPorts, outputPorts);
	}

	private List<Node>  		nodes = new ArrayList<Node>();
	private List<Connection> 	connections = new ArrayList<Connection>();
}
