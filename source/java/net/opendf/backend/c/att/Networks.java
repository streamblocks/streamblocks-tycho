package net.opendf.backend.c.att;

import java.util.ArrayList;
import java.util.List;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.att.Ports.PortKind;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

public class Networks extends Module<Networks.Required> {

	public interface Required {

		PortDecl declaration(Port p);

		List<Connection> connections(PortDecl p);

		Connection connection(PortDecl p);

		List<Connection> lookupConnectionsToPort(IRNode node, PortDecl decl);

		List<Connection> lookupConnectionsToNode(IRNode node, IRNode container, PortDecl port);

		PortKind portKind(PortDecl port);

		Node lookupNode(ActorMachine actorMachine);

	}
	
	@Synthesized
	public Node node(ActorMachine actorMachine) {
		return get().lookupNode(actorMachine);
	}
	
	@Inherited
	public Node lookupNode(Node node) {
		return node;
	}
	
	@Inherited
	public Node lookupNode(Object node) {
		return null;
	}

	@Synthesized
	public Connection connection(Port port) {
		return get().connection(get().declaration(port));
	}

	@Synthesized
	public List<Connection> connections(Port port) {
		return get().connections(get().declaration(port));
	}
	
	@Synthesized
	public Connection connection(PortDecl decl) {
		List<Connection> conns = get().connections(decl);
		if (conns.isEmpty()) return null;
		if (conns.size() > 1) throw new Error();
		return conns.get(0);
	}

	@Synthesized
	public List<Connection> connections(PortDecl decl) {
		return get().lookupConnectionsToPort(decl, decl);
	}
	
	@Inherited
	public List<Connection> lookupConnectionsToPort(Node node, PortDecl port) {
		return get().lookupConnectionsToNode(node, node, port);
	}
	
	@Inherited
	public List<Connection> lookupConnectionsToNode(Network net, IRNode container, PortDecl port) {
		List<Connection> result = new ArrayList<>();
		PortKind kind = get().portKind(port);
		if (kind == PortKind.INPUT) {
			for (Connection conn : net.getConnections()) {
				if (conn.getDstNodeId() == container.getIdentifier() && conn.getDstPort().getName().equals(port.getName())) {
					result.add(conn);
				}
			}
		} else if (kind == PortKind.OUTPUT) {
			for (Connection conn : net.getConnections()) {
				if (conn.getSrcNodeId() == container.getIdentifier() && conn.getSrcPort().getName().equals(port.getName())) {
					result.add(conn);
				}
			}
		}
		return result;
	}

}
