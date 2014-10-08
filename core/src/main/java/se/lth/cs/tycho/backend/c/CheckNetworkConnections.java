package se.lth.cs.tycho.backend.c;

import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.net.Connection;
import se.lth.cs.tycho.ir.net.Network;
import se.lth.cs.tycho.ir.net.Node;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class CheckNetworkConnections {
	public static boolean checkConnections(Network net) {
		for (Connection c : net.getConnections()) {
			if (c.getSrcNodeId() != null) {
				Node s = net.getNode(c.getSrcNodeId());
				if (!hasDeclForPort(s.getContent().getOutputPorts(), c.getSrcPort())) {
					return false;
				}
			} else {
				if (!hasDeclForPort(net.getInputPorts(), c.getSrcPort())) {
					return false;
				}
			}
			if (c.getDstNodeId() != null) {
				Node d = net.getNode(c.getDstNodeId());
				if (!hasDeclForPort(d.getContent().getInputPorts(), c.getDstPort())) {
					return false;
				}
			} else {
				if (!hasDeclForPort(net.getOutputPorts(), c.getDstPort())) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean hasDeclForPort(ImmutableList<PortDecl> decls, Port port) {
		for (PortDecl decl : decls) {
			if (decl.getName().equals(port.getName())) {
				return true;
			}
		}
		return false;
	}

}
