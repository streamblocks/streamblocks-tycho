package se.lth.cs.tycho.backend.c.att;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.analyze.Ports.PortKind;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortDecl;
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
		List<Connection> outgoingConnections(PortDecl p);

		@Synthesized
		Connection incomingConnection(PortDecl p);

		@Inherited
		List<Connection> lookupOutgoingConnections(IRNode node, PortDecl decl);

		@Inherited
		Connection lookupIncomingConnection(IRNode node, PortDecl decl);

		PortDecl portDeclaration(Port p);

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

	public Connection incomingConnection(PortDecl decl) {
		return e().lookupIncomingConnection(decl, decl);
	}

	public List<Connection> outgoingConnections(PortDecl decl) {
		return e().lookupOutgoingConnections(decl, decl);
	}
	
	public Connection lookupIncomingConnection(Network net, PortDecl port) {
		for (Connection conn : net.getConnections()) {
			PortDecl dst = e().portDeclaration(conn.getDstPort());
			if (dst == port) {
				return conn;
			}
		}
		throw new Error();
	}
	
	public List<Connection> lookupOutgoingConnections(Network net, PortDecl port) {
		List<Connection> result = new ArrayList<>();
		for (Connection conn : net.getConnections()) {
			PortDecl src = e().portDeclaration(conn.getSrcPort());
			if (src == port) {
				result.add(conn);
			}
		}
		return result;
	}

}
