package se.lth.cs.tycho.network.flatten.attr;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.network.flatten.attr.TreeRoot;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.entity.PortContainer;
import javarag.TreeTraverser;

public class NetworkTreeTraverser implements TreeTraverser<Object> {

	@Override
	public Iterable<? extends Object> getChildren(Object treeNode) {
		List<Object> result = new ArrayList<>();
		if (treeNode instanceof TreeRoot) {
			TreeRoot<?> root = (TreeRoot<?>) treeNode;
			result.add(root.getTree());
		} else if (treeNode instanceof Network) {
			Network net = (Network) treeNode;
			result.addAll(net.getInputPorts());
			result.addAll(net.getOutputPorts());
			result.addAll(net.getConnections());
			result.addAll(net.getNodes());
		} else if (treeNode instanceof Connection) {
			Connection conn = (Connection) treeNode;
			result.add(conn.getSrcPort());
			result.add(conn.getDstPort());
		} else if (treeNode instanceof Node) {
			Node node = (Node) treeNode;
			result.add(node.getIdentifier());
			result.add(node.getContent());
		} else if (treeNode instanceof PortContainer) {
			PortContainer portContainer = (PortContainer) treeNode;
			result.addAll(portContainer.getInputPorts());
			result.addAll(portContainer.getOutputPorts());
		}
		return result;
	}

}
