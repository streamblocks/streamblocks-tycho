package se.lth.cs.tycho.phases.cal2am;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class Priorities {
	private final CalActor actor;
	private final Map<QID, Node> graph;
	private final Map<QID, Set<QID>> isPrefixOf;

	public Priorities(CalActor actor) {
		this.actor = actor;
		this.graph = new HashMap<>();
		this.isPrefixOf = new HashMap<>();
		initIsPrefixOf();
		initNodes();
		initEdges();
	}

	private void initIsPrefixOf() {
		for (ImmutableList<QID> prioritySeq : actor.getPriorities()) {
			for (QID priorityTag : prioritySeq) {
				isPrefixOf.putIfAbsent(priorityTag, new HashSet<>());
				for (Action action : actor.getActions()) {
					QID actionTag = action.getTag();
					if (actionTag != null && priorityTag.isPrefixOf(actionTag)) {
						isPrefixOf.get(priorityTag).add(actionTag);
					}
				}
			}
		}
	}

	private void initNodes() {
		for (Action action : actor.getActions()) {
			graph.putIfAbsent(action.getTag(), new Node(action.getTag()));
		}
	}

	private void initEdges() {
		for (ImmutableList<QID> prioritySeq : actor.getPriorities()) {
			QID high = null;
			for (QID low : prioritySeq) {
				if (high != null) {
					for (QID highTag : isPrefixOf.get(high)) {
						for (QID lowTag : isPrefixOf.get(low)) {
							graph.get(highTag).addEdge(graph.get(lowTag));
						}
					}
				}
				high = low;
			}
		}
	}

	private class Node {
		private final QID tag;
		private final Set<Node> edges;

		public Node(QID tag) {
			this.tag = tag;
			this.edges = new HashSet<>();
		}

		public void addEdge(Node node) {
			edges.add(node);
		}

		public Set<Node> getReachable() {
			Set<Node> result = new HashSet<>();
			Queue<Node> queue = new ArrayDeque<>();
			queue.addAll(edges);
			while (!queue.isEmpty()) {
				Node n = queue.remove();
				if (result.add(n)) {
					queue.addAll(n.edges);
				}
			}
			return result;
		}

		public QID getTag() {
			return tag;
		}
	}

	public Set<QID> getPrioritized(Set<String> state, Set<QID> possibleTags) {
		Set<QID> reachable = possibleTags.stream()
				.map(graph::get)
				.flatMap(node -> node.getReachable().stream())
				.map(Node::getTag)
				.collect(Collectors.toSet());
		Set<QID> result = new HashSet<>(possibleTags);
		result.removeAll(reachable);
		return result;
	}
}
