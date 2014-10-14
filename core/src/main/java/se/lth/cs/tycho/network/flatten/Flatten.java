package se.lth.cs.tycho.network.flatten;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.network.flatten.attr.ConnectionPaths;
import se.lth.cs.tycho.network.flatten.attr.Connections;
import se.lth.cs.tycho.network.flatten.attr.NetworkTreeTraverser;
import se.lth.cs.tycho.network.flatten.attr.Ports;
import se.lth.cs.tycho.network.flatten.attr.TreeRoot;
import se.lth.cs.tycho.network.flatten.attr.TreeStructure;
import javarag.AttributeEvaluator;
import javarag.AttributeRegister;
import javarag.impl.reg.BasicAttributeRegister;
import se.lth.cs.tycho.network.flatten.AttributeMerger;

public class Flatten {
	private final Map<String, AttributeMerger> mergers = new HashMap<>();
	private static final NetworkTreeTraverser traverser = new NetworkTreeTraverser();
	
	private static class LazyHolder {
		private static final AttributeRegister register;
		static {
			register = new BasicAttributeRegister();
			register.register(TreeStructure.class, Ports.class,
					Connections.class, ConnectionPaths.class);
		}
	}

	public void registerAttributeMerger(String name, AttributeMerger merger) {
		if (mergers.containsKey(name)) {
			throw new IllegalStateException();
		}
		mergers.put(name, merger);
	}

	public Network flatten(Network network) {
		TreeRoot<Network> root = new TreeRoot<>(network);
		AttributeEvaluator evaluator = LazyHolder.register.getEvaluator(root, traverser);
		Set<List<Connection>> connectionPaths = evaluator.evaluate(
				"connectionPaths", root);
		ImmutableList<Connection> connections = merge(connectionPaths);
		Set<Node> nodes = evaluator.evaluate("actorNodes", root);
		ImmutableList<Node> nodeList = ImmutableList.copyOf(nodes);
		return network.copy(nodeList, connections, network.getInputPorts(),
				network.getOutputPorts());
	}

	private ImmutableList<Connection> merge(Set<List<Connection>> paths) {
		ImmutableList.Builder<Connection> builder = ImmutableList.builder();
		for (List<Connection> path : paths) {
			Connection first = path.get(0);
			Connection last = path.get(path.size()-1);
			builder.add(new Connection(first.getSrcNodeId(), first.getSrcPort(), last.getDstNodeId(), last.getDstPort(), null));
		}
		return builder.build();
	}
}
