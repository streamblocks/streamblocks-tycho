package net.opendf.backend.c;

import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.util.ImmutableList;

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
