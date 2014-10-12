package se.lth.cs.tycho.backend.c.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode.Identifier;

public class NetworkElementFinder {
	private final Network network;
	
	public NetworkElementFinder(Network network) {
		this.network = network;
	}

	public List<Node> getNodesByName(String name) {
		List<Node> result = new ArrayList<>();
		for (Node n : network.getNodes()) {
			if (name.equals(n.getName())) {
				result.add(n);
			}
		}
		return result;
	}
	
	public Node getNodeByName(String name) {
		return getSingletonElement(getNodesByName(name));
	}
	
	private <T> T getSingletonElement(List<T> list) {
		if (list.size() == 1) {
			return list.get(0);
		}
		throw new IllegalArgumentException("More than one elements.");
	}

	public List<Connection> getConnectionsByName(String srcNode, String srcPort, String dstNode, String dstPort) {
		List<Connection> result = new ArrayList<>();
		List<Node> src = srcNode == null ? Collections.<Node> singletonList(null) : getNodesByName(srcNode);
		List<Node> dst = dstNode == null ? Collections.<Node> singletonList(null) : getNodesByName(dstNode);
		for (Node s : src) {
			Identifier srcId = s == null ? null : s.getIdentifier();
			for (Node d : dst) {
				Identifier dstId = d == null ? null : d.getIdentifier();
				for (Connection c : network.getConnections()) {
					if (c.getSrcNodeId() == srcId && c.getDstNodeId() == dstId && c.getSrcPort().getName().equals(srcPort) && c.getDstPort().getName().equals(dstPort)) {
						result.add(c);
					}
				}
			}
		}
		return result;
	}
	
	public Connection getConnectionByName(String srcNode, String srcPort, String dstNode, String dstPort) {
		return getSingletonElement(getConnectionsByName(srcNode, srcPort, dstNode, dstPort));
	}

}
