package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.IRNode;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TreeContext {
	private final TreeContext parent;
	private final IRNode node;

	private TreeContext(TreeContext parent, IRNode node) {
		if (node == null) {
			throw new IllegalArgumentException("Node is null.");
		}
		this.parent = parent;
		this.node = node;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean hasParent() {
		return !isRoot();
	}

	public TreeContext getParent() {
		if (parent == null) {
			throw new NoSuchElementException("No parent of root.");
		}
		return parent;
	}

	public IRNode getNode() {
		return node;
	}

	public TreeContext forChild(IRNode node) {
		return new TreeContext(this, node);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parent) * 31 + node.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TreeContext) {
			return sameContext(this, (TreeContext) o);
		} else {
			return false;
		}
	}

	private static boolean sameContext(TreeContext a, TreeContext b) {
		if (a == b) {
			return true;
		} else if (a == null || b == null){
			return false;
		} else {
			return a.node == b.node && sameContext(a.parent, b.parent);
		}
	}

	public static TreeContext forRoot(IRNode root) {
		return new TreeContext(null, root);
	}

	public void traverseTree(Consumer<TreeContext> action) {
		action.accept(this);
		node.forEachChild(child -> forChild(child).traverseTree(action));
	}

	public IRNode transformTree(Function<TreeContext, IRNode> transformation) {
		return transformation.apply(this)
				.transformChildren(child -> forChild(child).transformTree(transformation));
	}
}
