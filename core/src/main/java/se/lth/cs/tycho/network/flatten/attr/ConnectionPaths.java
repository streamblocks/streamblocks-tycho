package se.lth.cs.tycho.network.flatten.attr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

		@Synthesized
		List<List<Connection>> pathsFromConnection(Connection conn);

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
		return outgoing.stream().flatMap(conn -> e().pathsFromConnection(conn).stream()).collect(Collectors.toList());
	}
	
	public List<List<Connection>> pathsFromConnection(Connection conn) {
		PortDecl dest = e().destinationPort(conn);
		List<List<Connection>> paths = e().pathsFromPort(dest);
		if (paths.isEmpty()) {
			return Collections.singletonList(Collections.singletonList(conn));
		} else {
			return paths.stream().map(path -> prepend(conn, path)).collect(Collectors.toList());
		}
	}
	
	private <E> List<E> prepend(E e, List<E> list) {
		List<E> result = new ArrayList<>();
		result.add(e);
		result.addAll(list);
		return result;
	}
}
