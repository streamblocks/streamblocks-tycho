package se.lth.cs.tycho.backend.c.att;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.backend.c.att.Ports.PortKind;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.net.Connection;
import se.lth.cs.tycho.ir.net.Network;
import se.lth.cs.tycho.ir.net.Node;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;

public class Networks extends Module<Networks.Decls> {

	public interface Decls {
		@Synthesized
		public Node node(ActorMachine actorMachine);

		@Inherited
		Node lookupNode(ActorMachine actorMachine);

		@Synthesized
		List<Connection> connections(PortDecl p);

		@Synthesized
		List<Connection> connections(Port p);

		@Synthesized
		Connection connection(PortDecl p);

		@Synthesized
		Connection connection(Port p);

		@Inherited
		List<Connection> lookupConnectionsToPort(IRNode node, PortDecl decl);

		@Inherited
		List<Connection> lookupConnectionsToNode(IRNode node, IRNode container, PortDecl port);

		PortDecl declaration(Port p);

		PortKind portKind(PortDecl port);

	}

	public Node node(ActorMachine actorMachine) {
		return e().lookupNode(actorMachine);
	}

	public Node lookupNode(Node node) {
		return node;
	}

	public Node lookupNode(Object node) {
		return null;
	}

	public Connection connection(Port port) {
		return e().connection(e().declaration(port));
	}

	public List<Connection> connections(Port port) {
		return e().connections(e().declaration(port));
	}

	public Connection connection(PortDecl decl) {
		List<Connection> conns = e().connections(decl);
		if (conns.isEmpty())
			return null;
		if (conns.size() > 1)
			throw new Error();
		return conns.get(0);
	}

	public List<Connection> connections(PortDecl decl) {
		return e().lookupConnectionsToPort(decl, decl);
	}

	public List<Connection> lookupConnectionsToPort(Node node, PortDecl port) {
		return e().lookupConnectionsToNode(node, node, port);
	}

	public List<Connection> lookupConnectionsToNode(Network net, IRNode container, PortDecl port) {
		List<Connection> result = new ArrayList<>();
		PortKind kind = e().portKind(port);
		if (kind == PortKind.INPUT) {
			for (Connection conn : net.getConnections()) {
				if (conn.getDstNodeId() == container.getIdentifier()
						&& conn.getDstPort().getName().equals(port.getName())) {
					result.add(conn);
				}
			}
		} else if (kind == PortKind.OUTPUT) {
			for (Connection conn : net.getConnections()) {
				if (conn.getSrcNodeId() == container.getIdentifier()
						&& conn.getSrcPort().getName().equals(port.getName())) {
					result.add(conn);
				}
			}
		}
		return result;
	}

}
