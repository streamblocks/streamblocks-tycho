package se.lth.cs.tycho.backend.c.test;

import se.lth.cs.tycho.backend.c.util.NetworkBuilder;
import se.lth.cs.tycho.ir.net.Network;

public class Fib100NetworkConstructor implements NetworkConstructor {

	@Override
	public Network constructNetwork(NodeReader reader) {
		NetworkBuilder builder = new NetworkBuilder();
		addPorts(builder);
		addNodes(builder, reader);
		addConnections(builder);
		return builder.build();
	}

	private void addPorts(NetworkBuilder builder) {
		builder.addOutputPort("Out", Util.intType());
	}

	private void addNodes(NetworkBuilder builder, NodeReader reader) {
		builder.addNode("add", reader.fromId("Add"));
		builder.addNode("init0", reader.fromId("Init0"));
		builder.addNode("init1", reader.fromId("Init1"));
		builder.addNode("take100", reader.fromId("Take100"));
		builder.addNode("id", reader.fromId("Id"));
	}

	private void addConnections(NetworkBuilder builder) {
		builder.addConnection("add", "Out", "take100", "In", Util.bufferSize(1));
		builder.addConnection("take100", "Out", "init1", "In", Util.bufferSize(1));
		builder.addConnection("init1", "Out", "id", "In", Util.bufferSize(1));
		builder.addConnection("init1", "Out", "add", "InA", Util.bufferSize(1));
		builder.addConnection("init1", "Out", "init0", "In", Util.bufferSize(1));
		builder.addConnection("init0", "Out", "add", "InB", Util.bufferSize(1));
		builder.addConnection("id", "Out", null, "Out");
	}

}
