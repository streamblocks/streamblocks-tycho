package se.lth.cs.tycho.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

/**
 * Directed Acyclic Graph where nodes are the integers from <code>0</code> to
 * <code>numberOfNodes() - 1</code>.
 */
public class DAG {

	/**
	 * Exception indicating that the graph has a cycle.
	 */
	public static class CyclicGraphException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private final BitSet[] arcs;
	private final BitSet[] reachableNodes;

	private DAG(BitSet[] arcs) {
		this.arcs = arcs;
		this.reachableNodes = new BitSet[arcs.length];
		for (int n = 0; n < arcs.length; n++) {
			reachableNodes[n] = new BitSet();
		}
		int[] top = topologicalOrdering();
		for (int i = top.length - 1; i >= 0; i--) {
			int src = top[i];
			BitSet closure = reachableNodes[src];
			for (int dst = arcs[src].nextSetBit(0); dst >= 0; dst = arcs[src].nextSetBit(dst + 1)) {
				closure.set(dst);
				closure.or(reachableNodes[dst]);
			}
		}
	}

	/**
	 * Keeps all nodes of <code>nodes</code> for which there exist no path to
	 * another node in <code>nodes</code>, and removes the other nodes.
	 * 
	 * @param nodes
	 */
	public void keepLeavesOf(BitSet nodes) {
		for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node + 1)) {
			if (!isLeafInSubgraph(node, nodes)) {
				nodes.clear(node);
			}
		}
	}

	private boolean isLeafInSubgraph(int node, BitSet nodes) {
		return !reachableNodes[node].intersects(nodes);
	}

	/**
	 * Sets <code>result</code> to be the nodes that are reachable from
	 * <code>node</code>.
	 * 
	 * @param node
	 * @param result
	 */
	public void getReachableNodes(int node, BitSet result) {
		result.clear();
		result.or(reachableNodes[node]);
	}

	/**
	 * Sets <code>result</code> to be all nodes with an arc from
	 * <code>node</code>.
	 * 
	 * @param node
	 * @param result
	 */
	public void getSuccessors(int node, BitSet result) {
		result.clear();
		result.or(arcs[node]);
	}

	/**
	 * Returns a topological ordering of the nodes in the graph.
	 * 
	 * @return a topological ordering
	 */
	public int[] topologicalOrdering() {
		int size = numberOfNodes();
		int[] result = new int[size];
		BitSet visited = new BitSet(size);
		BitSet visiting = new BitSet(size);
		int next = size - 1;
		while (next >= 0) {
			topVisit(next, visited, visiting, result);
			next = visited.previousClearBit(next - 1);
		}
		return result;
	}

	private void topVisit(int node, BitSet visited, BitSet visiting, int[] sorted) {
		if (visiting.get(node)) {
			throw new CyclicGraphException();
		}
		if (!visited.get(node)) {
			BitSet children = arcs[node];
			visiting.set(node);
			for (int child = children.previousSetBit(numberOfNodes() - 1); child >= 0; child = children
					.previousSetBit(child - 1)) {
				topVisit(child, visited, visiting, sorted);
			}
			visiting.clear(node);
			visited.set(node);
			sorted[sorted.length - visited.cardinality()] = node;
		}
	}

	/**
	 * Returns all arcs in the graph.
	 * 
	 * @return Returns all arcs in the graph.
	 */
	public Iterable<Arc> arcs() {
		return new ArcIterable();
	}

	/**
	 * Returns the number of nodes in the graph.
	 * 
	 * @return Returns the number of nodes in the graph.
	 * 
	 */
	public int numberOfNodes() {
		return arcs.length;
	}

	/**
	 * Returns the number of arcs in the graph.
	 * 
	 * @return Returns the number of arcs in the graph.
	 */
	public int numberOfArcs() {
		int count = 0;
		for (BitSet as : arcs) {
			count += as.cardinality();
		}
		return count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arcs);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DAG))
			return false;
		DAG other = (DAG) obj;
		if (!Arrays.equals(arcs, other.arcs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("({");
		for (int n = 0; n < numberOfNodes(); n++) {
			if (n > 0) sb.append(", ");
			sb.append(n);
		}
		sb.append("}, {");
		boolean first = true;
		for (Arc arc : arcs()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append("(").append(arc).append(")");
		}
		sb.append("})");
		return sb.toString();
	}

	private class ArcIterable implements Iterable<Arc> {
		@Override
		public Iterator<Arc> iterator() {
			return new ArcIterator();
		}
	}

	private class ArcIterator implements Iterator<Arc> {
		final int size;
		int src;
		int dst;

		public ArcIterator() {
			size = numberOfNodes();
			src = 0;
			dst = -1;
			advance();
		}

		private void advance() {
			dst = arcs[src].nextSetBit(dst + 1);
			while (dst < 0 && src < size) {
				src += 1;
				if (src < size) {
					dst = arcs[src].nextSetBit(dst + 1);
				}
			}
		}

		@Override
		public boolean hasNext() {
			return src < size;
		}

		@Override
		public Arc next() {
			if (src >= size) {
				throw new IllegalStateException();
			}
			Arc result = new Arc(src, dst);
			advance();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Builder for DAGs
	 */
	public static class Builder {
		private BitSet[] arcs;
		private boolean dirty;

		/**
		 * Constructs a builder for a graph with <code>size</code> nodes.
		 * 
		 * @param size number of nodes in the graph
		 */
		public Builder(int size) {
			this.arcs = new BitSet[size];
			this.dirty = false;
		}

		/**
		 * Adds an arc to the graph that is built.
		 * 
		 * @param source
		 * @param destination
		 */
		public void addArc(int source, int destination) {
			checkBounds(source);
			checkBounds(destination);
			cloneIfDirty();
			if (arcs[source] == null) {
				arcs[source] = new BitSet();
			}
			arcs[source].set(destination);
		}

		private void checkBounds(int node) {
			if (node >= arcs.length || node < 0) {
				throw new IndexOutOfBoundsException(Integer.toString(node));
			}
		}

		private void cloneIfDirty() {
			if (dirty) {
				BitSet[] clone = new BitSet[arcs.length];
				for (int n = 0; n < arcs.length; n++) {
					if (arcs[n] != null && !arcs[n].isEmpty()) {
						clone[n] = new BitSet();
						clone[n].or(arcs[n]);
					}
				}
				arcs = clone;
				dirty = false;
			}
		}

		/**
		 * Builds and returns the graph.
		 * 
		 * @return the graph
		 * @throws CyclicGraphException
		 */
		public DAG build() {
			dirty = true;
			BitSet empty = new BitSet();
			for (int n = 0; n < arcs.length; n++) {
				if (arcs[n] == null) {
					arcs[n] = empty;
				}
			}
			return new DAG(arcs);
		}
	}

	/**
	 * An arc from a node to a node.
	 */
	public static class Arc {
		private final int src;
		private final int dst;

		/**
		 * Constructs an <code>Arc</code> from <code>source</code> to
		 * <code>destination</code>.
		 * 
		 * @param source
		 * @param destination
		 */
		public Arc(int source, int destination) {
			this.src = source;
			this.dst = destination;
		}

		/**
		 * Returns the source of the arc.
		 * 
		 * @return the source
		 */
		public int getSource() {
			return src;
		}

		/**
		 * Returns the destination of the arc.
		 * 
		 * @return the destination
		 */
		public int getDestination() {
			return dst;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dst;
			result = prime * result + src;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Arc))
				return false;
			Arc other = (Arc) obj;
			if (dst != other.dst)
				return false;
			if (src != other.src)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return src + " -> " + dst;
		}
	}
}
