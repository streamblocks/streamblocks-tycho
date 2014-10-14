package se.lth.cs.tycho.network.flatten.attr;

import java.util.Set;

import se.lth.cs.tycho.network.flatten.attr.Connections;
import se.lth.cs.tycho.network.flatten.attr.Ports;
import se.lth.cs.tycho.network.flatten.attr.TreeStructure;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.IRNode.Identifier;
import se.lth.cs.tycho.ir.entity.PortDecl;
import javarag.Collected;
import javarag.Module;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.Builders;
import javarag.coll.Collector;

public class Connections extends Module<Connections.Attributes> {

	public interface Attributes extends Ports.Attributes, TreeStructure.Attributes {
		@Synthesized
		PortDecl sourcePort(Connection conn);

		@Synthesized
		PortDecl destinationPort(Connection conn);

		@Collected
		Set<Connection> outgoingConnections(PortDecl port);

	}

	public PortDecl sourcePort(Connection conn) {
		Identifier id = conn.getSrcNodeId();
		Port port = conn.getSrcPort();
		if (id != null) {
			Node node = e().enclosingNode(id);
			return e().outputPortDecl(node.getContent(), port);
		} else {
			Network net = e().enclosingNetwork(conn);
			return e().inputPortDecl(net, port);
		}
	}

	public PortDecl destinationPort(Connection conn) {
		Identifier id = conn.getDstNodeId();
		Port port = conn.getDstPort();
		if (id != null) {
			Node node = e().enclosingNode(id);
			return e().inputPortDecl(node.getContent(), port);
		} else {
			Network net = e().enclosingNetwork(conn);
			return e().outputPortDecl(net, port);
		}
	}

	public Builder<Set<Connection>, Connection> outgoingConnections(
			PortDecl port) {
		return Builders.setBuilder();
	}

	public void outgoingConnections(Connection conn, Collector<Connection> coll) {
		PortDecl port = e().sourcePort(conn);
		coll.add(port, conn);
	}
}
