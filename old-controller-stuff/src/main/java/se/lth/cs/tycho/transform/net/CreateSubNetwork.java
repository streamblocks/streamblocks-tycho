package se.lth.cs.tycho.transform.net;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.instance.net.Node.Identifier;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class CreateSubNetwork {
	
	private final Network network;
	private final String name;
	private final Collection<Node> nodes;
	private final Collection<Connection> connections;
	
	public CreateSubNetwork(Network network, String name, Collection<Node> nodes, Collection<Connection> connections) {
		this.network = network;
		this.name = name;
		this.nodes = nodes;
		this.connections = connections;
	}

	public static Network createSubNetwork(Network net, String name, Collection<Node> nodes,
			Collection<Connection> connections) {
		CreateSubNetwork instance = new CreateSubNetwork(net, name, nodes, connections);
		return instance.build();
	}

	private Network build() {
		Map<NodePort, String> inputPorts = new LinkedHashMap<>();
		Map<NodePort, String> outputPorts = new LinkedHashMap<>();
		buildPortMaps(inputPorts, outputPorts);
		Network subnet = subNetwork(inputPorts, outputPorts);
		return network(inputPorts, outputPorts, subnet);
	}
	
	private Network network(Map<NodePort, String> inputPorts, Map<NodePort, String> outputPorts, Network subnet) {
		Node subnetNode = new Node(name, subnet);
		ImmutableList<Connection> connectionList = connections(inputPorts, outputPorts, subnetNode);
		ImmutableList<Node> nodeList = nodes(subnetNode);
		return network.copy(nodeList, connectionList, network.getInputPorts(), network.getOutputPorts());
	}

	private ImmutableList<Node> nodes(Node subnetNode) {
		ImmutableList.Builder<Node> nodeList = ImmutableList.builder();
		for (Node n : network.getNodes()) {
			if (!nodes.contains(n)) {
				nodeList.add(n);
			}
		}
		nodeList.add(subnetNode);
		return nodeList.build();
	}

	private ImmutableList<Connection> connections(Map<NodePort, String> inputPorts,
			Map<NodePort, String> outputPorts, Node subnetNode) {
		ImmutableList.Builder<Connection> connectionList = ImmutableList.builder();
		for (Connection connection : network.getConnections()) {
			if (!connections.contains(connection)) {
				Identifier src = connection.getSrcNodeId();
				Port srcPort = connection.getSrcPort();
				Identifier dst = connection.getDstNodeId();
				Port dstPort = connection.getDstPort();
				NodePort s = new NodePort(src, srcPort.getName());
				NodePort d = new NodePort(dst, dstPort.getName());
				if (inputPorts.containsKey(d)) {
					dst = subnetNode.getIdentifier();
					dstPort = new Port(inputPorts.get(s));
				}
				if (outputPorts.containsKey(s)) {
					src = subnetNode.getIdentifier();
					srcPort = new Port(outputPorts.get(s));
				}
				connections.add(connection.copy(src, srcPort, dst, dstPort, connection.getToolAttributes()));
			}
		}
		return connectionList.build();
	}

	private void buildPortMaps(Map<NodePort, String> inputPorts, Map<NodePort, String> outputPorts) {
		Set<Identifier> nodeIds = new HashSet<>();
		for (Node n : nodes) {
			nodeIds.add(n.getIdentifier());
		}

		Set<String> inputPortNames = new HashSet<>();
		Set<String> outputPortNames = new HashSet<>();
		for (Connection conn : connections) {
			if (nodeIds.contains(conn.getSrcNodeId())) {
				addPort(conn.getSrcNodeId(), conn.getSrcPort().getName(), outputPorts, outputPortNames);
			}
			if (nodeIds.contains(conn.getDstNodeId())) {
				addPort(conn.getDstNodeId(), conn.getDstPort().getName(), inputPorts, inputPortNames);
			}
		}
	}

	private void addPort(Identifier node, String port, Map<NodePort, String> ports,
			Set<String> portNames) {
		String uniqueName = getUniqueName(port, portNames);
		portNames.add(uniqueName);
		ports.put(new NodePort(node, port), uniqueName);
	}

	private String getUniqueName(String name, Set<String> existing) {
		if (existing.contains(name)) {
			int i = 0;
			String newName;
			do {
				i += 1;
				newName = name + "-" + i;
			} while (existing.contains(newName));
			return newName;
		}
		return name;
	}
	
	private ImmutableList<PortDecl> portDecls(Collection<String> portNames) {
		ImmutableList.Builder<PortDecl> builder = ImmutableList.builder();
		for (String name : portNames) {
			builder.add(new PortDecl(name));
		}
		return builder.build();
	}

	private Network subNetwork(Map<NodePort, String> inputPorts, Map<NodePort, String> outputPorts) {
		ImmutableList<PortDecl> inputPortDecls = portDecls(inputPorts.values());
		ImmutableList<PortDecl> outputPortDecls = portDecls(outputPorts.values());
		ImmutableList.Builder<Connection> allConnections = ImmutableList.builder();
		allConnections.addAll(connections);
		for (Map.Entry<NodePort, String> input : inputPorts.entrySet()) {
			Port srcPort = new Port(input.getValue());
			Identifier dstNodeId = input.getKey().getNode();
			Port dstPort = new Port(input.getKey().getPort());
			allConnections.add(new Connection(null, srcPort, dstNodeId, dstPort, null));
		}
		for (Map.Entry<NodePort, String> output : outputPorts.entrySet()) {
			Identifier srcNodeId = output.getKey().getNode();
			Port srcPort = new Port(output.getKey().getPort());
			Port dstPort = new Port(output.getValue());
			allConnections.add(new Connection(srcNodeId, srcPort, null, dstPort, null));
		}
		return new Network(ImmutableList.from(nodes), allConnections.build(), inputPortDecls, outputPortDecls);
	}

	private static class NodePort {
		private final Identifier node;
		private final String port;

		public NodePort(Identifier node, String port) {
			this.node = node;
			this.port = port;
		}

		public Identifier getNode() {
			return node;
		}

		public String getPort() {
			return port;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((node == null) ? 0 : node.hashCode());
			result = prime * result + ((port == null) ? 0 : port.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof NodePort)) {
				return false;
			}
			NodePort other = (NodePort) obj;
			if (node == null) {
				if (other.node != null) {
					return false;
				}
			} else if (!node.equals(other.node)) {
				return false;
			}
			if (port == null) {
				if (other.port != null) {
					return false;
				}
			} else if (!port.equals(other.port)) {
				return false;
			}
			return true;
		}

	}

}
