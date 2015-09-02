package se.lth.cs.tycho.transform.net;

import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.Transformation;

public class NetworkUtils {
	public static Network transformNodes(Network network, Transformation<Node> transformation) {
		return network.copy(
				network.getNodes().stream()
						.map((Node node) -> {
								Node transformed = transformation.apply(node);
								return node.copy(
										transformed.getName(),
										transformed.getContent());
						})
						.collect(ImmutableList.collector()),
				network.getConnections(),
				network.getInputPorts(),
				network.getOutputPorts());
	}
}
