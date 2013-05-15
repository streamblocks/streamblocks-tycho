package net.opendf.transform.caltoam.util;

import java.util.BitSet;

/**
 * Directed acyclic graph with the natural numbers as nodes.
 */
public class IntDAG {
	private BitSet[] transitiveClosure;
	private BitSet[] transitiveClosureInverse;
	private final int size;

	/**
	 * Constructs a graph with vertices 0 to size-1 and no edges.
	 * 
	 * @param size
	 *            number of nodes
	 */
	public IntDAG(int size) {
		this.size = size;
		transitiveClosure = new BitSet[size];
		transitiveClosureInverse = new BitSet[size];
		for (int i = 0; i < size; i++) {
			transitiveClosure[i] = new BitSet(size);
			transitiveClosureInverse[i] = new BitSet(size);
		}
	}

	/**
	 * Adds an edge to the graph from source to destination if that edge does
	 * not introduce a cycle in the graph. Returns true if the edge was added.
	 * 
	 * @param source
	 *            the source node
	 * @param destination
	 *            the destination node
	 * @return true if the edge was added
	 */
	public boolean addEdge(int source, int destination) {
		checkBounds(source);
		checkBounds(destination);

		if (transitiveClosureInverse[source].get(destination) || source == destination) {
			return false;
		}

		BitSet sources = new BitSet(size);
		sources.or(transitiveClosureInverse[source]);
		sources.set(source);

		BitSet destinations = new BitSet(size);
		destinations.or(transitiveClosure[destination]);
		destinations.set(destination);

		for (int s = sources.nextSetBit(0); s >= 0; s = sources.nextSetBit(s + 1)) {
			transitiveClosure[s].or(destinations);
		}

		for (int d = destinations.nextSetBit(0); d >= 0; d = destinations.nextSetBit(d + 1)) {
			transitiveClosureInverse[d].or(sources);
		}
		return true;
	}

	/**
	 * Removed all but the topmost vertices among those in nodes.
	 * 
	 * Let T be the transitive closure of this graph, and let S be the subgraph
	 * of T induced by nodes. This method returns the roots of S.
	 * 
	 * @param nodes
	 *            the nodes of interest
	 */
	public void keepRoots(BitSet nodes) {
		for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node + 1)) {
			if (!isRootInSubgraph(node, nodes)) {
				nodes.clear(node);
			}
		}
	}

	private boolean isRootInSubgraph(int node, BitSet nodes) {
		return !transitiveClosureInverse[node].intersects(nodes);
	}

	private void checkBounds(int i) {
		if (i < 0 || i >= size) {
			throw new IndexOutOfBoundsException();
		}
	}
}
