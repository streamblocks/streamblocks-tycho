package se.lth.cs.tycho.network.flatten.attr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import se.lth.cs.tycho.network.flatten.attr.ConnectionPaths;
import se.lth.cs.tycho.network.flatten.attr.Connections;
import se.lth.cs.tycho.network.flatten.attr.TreeRoot;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.ir.entity.PortDecl;
import javarag.Collected;
import javarag.Module;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.Builders;
import javarag.coll.Collector;

public class ConnectionPaths extends Module<ConnectionPaths.Attributes> {

	public interface Attributes extends Connections.Attributes {
		@Synthesized
		List<List<Connection>> pathsFromPort(PortDecl port);

		@Collected
		Set<List<List<Connection>>> connectionPaths(TreeRoot<?> root);
	}

	public Builder<Set<List<Connection>>, List<Connection>> connectionPaths(TreeRoot<?> root) {
		return Builders.setBuilder();
	}
	
	public void connectionPaths(PortDecl port, Collector<List<Connection>> coll) {
		TreeRoot<?> root = e().treeRoot(port);
		for (List<Connection> path : e().pathsFromPort(port)) {
			coll.add(root, path);
		}
	}

	public List<List<Connection>> pathsFromPort(PortDecl port) {
		Set<Connection> outgoing = e().outgoingConnections(port);
		List<List<Connection>> result = new ArrayList<>();
		for (Connection c : outgoing) {
			PortDecl dest = e().destinationPort(c);
			List<List<Connection>> paths = e().pathsFromPort(dest);
			prependAndCollect(c, paths, result);
		}
		return result;
	}

	private <C> void prependAndCollect(C c, List<List<C>> paths,
			List<List<C>> result) {
		for (List<C> path : paths) {
			List<C> newPath = new ArrayList<>();
			newPath.add(c);
			newPath.addAll(path);
			result.add(newPath);
		}
	}
}
