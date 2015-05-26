package se.lth.cs.tycho.backend.c;

import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.instance.net.Node.Identifier;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class NetworkFunctions {
	public static Network fromSingleNode(Instance instance, String name) {
		Node node = new Node(name, instance, null);
		ImmutableList<PortDecl> inputPorts = copyPorts(instance.getInputPorts());
		ImmutableList<PortDecl> outputPorts = copyPorts(instance.getOutputPorts());
		ImmutableList.Builder<Connection> builder = ImmutableList.builder();
		for (PortDecl port : instance.getInputPorts()) {
			String portName = port.getName();
			Identifier identifier = node.getIdentifier();
			Connection conn = new Connection(null, new Port(portName), identifier, new Port(portName), null);
			builder.add(conn);
		}
		for (PortDecl port : instance.getOutputPorts()) {
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
