package net.opendf.transform.reduction;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opendf.transform.util.GenInstruction;
import net.opendf.transform.util.GenInstruction.Visitor;
import net.opendf.transform.util.StateHandler;

public class ShortestPathStateHandler<S> implements StateHandler<S> {

	private final GenInstruction.Visitor<S, Integer, Void> weight;
	private final StateHandler<S> stateHandler;
	private final Map<S, GenInstruction<S>> decisions;

	public ShortestPathStateHandler(Visitor<S, Integer, Void> weight, StateHandler<S> stateHandler) {
		this.weight = weight;
		this.stateHandler = stateHandler;
		decisions = new HashMap<>();
	}

	@Override
	public List<GenInstruction<S>> getInstructions(S state) {
		if (!decisions.containsKey(state)) {
			List<S> states = topSortBasicSection(state);
			Map<S, Edge> pred = new HashMap<>();
			Edge callEdge = null;
			Edge waitEdge = null;
			for (S s : states) {
				final int dist = pred.containsKey(s) ? pred.get(s).distance : 0;
				for (GenInstruction<S> i : stateHandler.getInstructions(s)) {
					if (i.isTest()) {
						for (S dest : i.destinations()) {
							int d = dist + i.accept(weight);
							if (pred.containsKey(dest)) {
								Edge e = pred.get(dest);
								if (e.distance <= d) {
									continue;
								}
							}
							pred.put(dest, new Edge(s, i, d));
						}
					} else if (i.isCall()) {
						int d = dist + i.accept(weight);
						if (callEdge == null || d < callEdge.distance) {
							callEdge = new Edge(s, i, d);
						}
					} else if (i.isWait()) {
						int d = dist + i.accept(weight);
						if (waitEdge == null || d < waitEdge.distance) {
							waitEdge = new Edge(s, i, d);
						}
					}
				}
			}

			Edge edge = callEdge != null ? callEdge : waitEdge;
			while (edge != null) {
				decisions.put(edge.source, edge.edge);
				edge = pred.get(edge.source);
			}
		}
		return Collections.singletonList(decisions.get(state));
	}

	private class Edge {
		public final S source;
		public final GenInstruction<S> edge;
		public final int distance;

		public Edge(S source, GenInstruction<S> edge, int distance) {
			this.source = source;
			this.edge = edge;
			this.distance = distance;
		}
	}

	@Override
	public S initialState() {
		return stateHandler.initialState();
	}

	private List<S> topSortBasicSection(S state) {
		LinkedList<S> result = new LinkedList<>();
		Set<S> visited = new HashSet<>();
		topSortUtil(state, result, visited);
		return result;
	}

	private void topSortUtil(S state, LinkedList<S> result, Set<S> visited) {
		if (!visited.contains(state)) {
			visited.add(state);
			for (GenInstruction<S> i : stateHandler.getInstructions(state)) {
				if (i.isTest()) {
					for (S d : i.destinations()) {
						topSortUtil(d, result, visited);
					}
				}
			}
			result.addLast(state);
		}
	}

}
