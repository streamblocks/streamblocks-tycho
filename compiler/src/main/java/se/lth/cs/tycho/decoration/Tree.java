package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.IRNode;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A tree class that represents a tree of IRNodes and a position in that tree. The type parameter denotes the type of
 * the node pointed at.
 * @param <N> type of the node pointed at
 */
public final class Tree<N extends IRNode> {
	private final Tree<? extends IRNode> parent;
	private final N node;

	private Tree(Tree<? extends IRNode> parent, N node) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null.");
		}
		this.parent = parent;
		this.node = node;
	}

	/**
	 * Returns an Optional with a tree pointing at the parent of the current node, or an empty Optional if the current
	 * node is the root.
	 * @return the parent of the current node
	 * @throws NoSuchElementException if the node is the root
	 */
	public Optional<Tree<? extends IRNode>> parent() {
		return Optional.ofNullable(parent);
	}

	/**
	 * Returns a Stream starting with this node continuing with all parents with the closes parent first.
	 * @return the sequence of parents
	 */
	public Stream<Tree<? extends IRNode>> parentChain() {
		Stream.Builder<Tree<? extends IRNode>> builder = Stream.builder();
		Tree<? extends IRNode> tree = this;
		while (tree != null) {
			builder.add(tree);
			tree = tree.parent;
		}
		return builder.build();
	}

	/**
	 * Returns the node that this tree is pointing at.
	 * @return the current node
	 */
	public N node() {
		return node;
	}

	/**
	 * Returns an Optional with the closest parent that statisfies the given predicate or an empty Optional if no such parent is found.
	 * @param predicate a predicate identifing the parent to look for
	 * @return the closest parent that satisfies the given predicate
	 */
	public Optional<Tree<? extends IRNode>> findParent(Predicate<IRNode> predicate) {
		Tree<? extends IRNode> parent = this;
		while (parent.parent().isPresent()) {
			parent = parent.parent().get();
			if (predicate.test(parent.node())) {
				return Optional.of(parent);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns an Optional with the closest parent of the given type or an empty Optional if no such parent is found.
	 * @param type a Class object representing the parent type
	 * @param <M> the type of the parent
	 * @return the closes parent of the given type.
	 */
	@SuppressWarnings("unchecked")
	public <M extends IRNode> Optional<Tree<M>> findParentOfType(Class<M> type) {
		return (Optional) findParent(type::isInstance);
	}

	/**
	 * Returns a tree where the given node is a child of the current node.
	 * @param node the new child
	 * @param <M> the type of the child
	 * @return a tree pointing at the given child
	 */
	private <M extends IRNode> Tree<M> withChild(M node) {
		return new Tree<>(this, node);
	}


	private <M extends IRNode> Tree<M> withNode(M node) {
		return new Tree<>(parent, node);
	}

	/**
	 * Extends the root of the tree with the given tree. The node pointed at by the given tree must be the root node of
	 * this tree.
	 * @param root the root
	 * @return an tree that is extended with the given root
	 * @throws IllegalArgumentException if the root node of this tree is not the node pointed at by the given tree
	 */
	public Tree<N> attachTo(Tree<? extends IRNode> root) {
		if (parent == null) {
			if (root.node != node) {
				throw new IllegalArgumentException("Not a root of this tree.");
			}
			return (Tree<N>) root;
		} else {
			return parent.attachTo(root).withChild(node);
		}
	}

	public <M extends IRNode> Tree<M> child(Function<N, M> getChild) {
		return withChild(getChild.apply(node));
	}

	public <M extends IRNode> Stream<Tree<M>> children(Function<N, Collection<M>> getChildren) {
		return getChildren.apply(node).stream().map(this::withChild);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parent, node);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Tree<?>) {
			return sameTree(this, (Tree<?>) o);
		} else {
			return false;
		}
	}

	private static boolean sameTree(Tree<?> a, Tree<?> b) {
		if (a == b) {
			return true;
		} else if (a == null || b == null){
			return false;
		} else {
			return a.node == b.node && sameTree(a.parent, b.parent);
		}
	}

	@Override
	public String toString() {
		return parentChain()
				.map((Tree<? extends IRNode> tree) -> (IRNode) tree.node())
				.map(node -> node.toString())
				.collect(Collectors.joining(" -> ", "Tree(", ")"));
	}

	/**
	 * Returns a tree that is rooted in the given node.
	 * @param root the root node of the tree
	 * @param <N> the type of the root node
	 * @return a tree with the given root
	 */
	public static <N extends IRNode> Tree<N> of(N root) {
		return new Tree<>(null, root);
	}

	@SuppressWarnings("unchecked")
	public <M extends IRNode> Optional<Tree<M>> tryCast(Class<M> type) {
		if (type.isInstance(node)) {
			return Optional.of((Tree) this);
		} else {
			return Optional.empty();
		}
	}

	public static <To extends IRNode, From extends To> Tree<To> upCast(Tree<From> tree) {
		return (Tree) tree;
	}

	public <M extends IRNode> Tree<M> assertNode(M node) {
		if (this.node == node) {
			return (Tree<M>) this;
		} else {
			throw new AssertionError("this.node() != node");
		}
	}

	public IRNode transformNodes(Function<Tree<? extends IRNode>, IRNode> transformation) {
		HashMap<Tree<IRNode>, IRNode> transformedChildren = new HashMap<>();
		node.forEachChild(child -> {
			Tree<IRNode> node = withChild(child);
			transformedChildren.put(node, node.transformNodes(transformation));
		});
		IRNode transformedNode = transformation.apply(this);
		return transformedNode.transformChildren(child -> {
			Tree<IRNode> node = withChild(child);
			if (!transformedChildren.containsKey(node)) {
				throw new IllegalStateException("A child node was possibly transformed twice.");
			}
			return transformedChildren.get(node);
		});
	}

	public void forEachChild(Consumer<Tree<? extends IRNode>> action) {
		node.forEachChild(child -> action.accept(withChild(child)));
	}

	public void transformChildren(Function<Tree<? extends IRNode>, IRNode> transformation) {
		node.transformChildren(child -> transformation.apply(withChild(child)));
	}

	public Stream<Tree<IRNode>> walk() {
		TreeIterator iterator = new TreeIterator((Tree<IRNode>) this);
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.IMMUTABLE), false);
	}

	private static class TreeIterator implements Iterator<Tree<IRNode>> {
		private final ArrayDeque<Tree<IRNode>> queue;
		private final ArrayDeque<Tree<IRNode>> reverseList;

		public TreeIterator(Tree<IRNode> root) {
			queue = new ArrayDeque<>();
			reverseList = new ArrayDeque<>();
			queue.addLast(root);
		}

		@Override
		public boolean hasNext() {
			return !queue.isEmpty();
		}

		@Override
		public Tree<IRNode> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Tree<IRNode> next = queue.removeLast();
			assert reverseList.isEmpty();
			next.node().forEachChild(child -> reverseList.addLast(next.withChild(child)));
			while (!reverseList.isEmpty()) {
				queue.addLast(reverseList.removeLast());
			}
			return next;
		}
	}

}
