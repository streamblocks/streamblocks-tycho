package net.opendf.backend.c;

import net.opendf.ir.Port;
import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.entity.PortContainer;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.util.ImmutableList;

public class NetworkFunctions {
	public static Network fromSingleNode(PortContainer actorMachine, String name) {
		Node node = new Node(name, actorMachine, null);
		ImmutableList<PortDecl> inputPorts = copyPorts(actorMachine.getInputPorts());
		ImmutableList<PortDecl> outputPorts = copyPorts(actorMachine.getOutputPorts());
		ImmutableList.Builder<Connection> builder = ImmutableList.builder();
		for (PortDecl port : actorMachine.getInputPorts()) {
			String portName = port.getName();
			Identifier identifier = node.getIdentifier();
			Connection conn = new Connection(null, new Port(portName), identifier, new Port(portName), null);
			builder.add(conn);
		}
		for (PortDecl port : actorMachine.getOutputPorts()) {
			String portName = port.getName();
			Identifier identifier = node.getIdentifier();
			Connection conn = new Connection(identifier, new Port(portName), null, new Port(portName), null);
			builder.add(conn);
		}
		return new Network(ImmutableList.of(node), builder.build(), inputPorts, outputPorts);
	}
	
	private static ImmutableList<PortDecl> copyPorts(ImmutableList<PortDecl> decls) {
		ImmutableList.Builder<PortDecl> builder = ImmutableList.builder();
		for (PortDecl decl : decls) {
			builder.add(new PortDecl(decl.getName(), null));
		}
		return builder.build();
	}

}
