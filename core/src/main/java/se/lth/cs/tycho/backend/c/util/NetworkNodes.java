package se.lth.cs.tycho.backend.c.util;

import java.util.ArrayList;
import java.util.List;

import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.net.Network;
import se.lth.cs.tycho.ir.net.Node;

public class NetworkNodes {
	public static List<Node> getByName(Network network, String nodeName) {
		return getByPath(network.getNodes(), new String[] {nodeName}, 0);
	}
	
	public static List<Node> getByName(Network network, String... path) {
		return getByPath(network.getNodes(), path, 0);
	}

	private static List<Node> getByPath(List<Node> nodes, String[] path, int index) {
		List<Node> result = new ArrayList<>();
		if (index >= path.length) {
			return result;
		}
		for (Node n : nodes) {
			if (path[index].equals(n.getName())) {
				if (index == path.length - 1) {
					result.add(n);
				} else {
					PortContainer content = n.getContent();
					if (content instanceof Network) {
						Network subNetwork = (Network) content;
						List<Node> subNodes = getByPath(subNetwork.getNodes(), path, index+1);
						result.addAll(subNodes);
					}
				}
			}
		}
		return result;
	}

}
