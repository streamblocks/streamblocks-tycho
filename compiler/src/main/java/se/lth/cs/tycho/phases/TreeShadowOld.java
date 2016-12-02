package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.ir.IRNode;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class TreeShadowOld implements TreeShadow {
	private final IRNode root;
	private final Map<IRNode, IRNode> parentMap;

	private TreeShadowOld(IRNode root, Map<IRNode, IRNode> parentMap) {
		this.root = root;
		this.parentMap = parentMap;
	}

	public static TreeShadowOld of(IRNode root) {
		Builder builder = new Builder();
		builder.accept(root);
		return new TreeShadowOld(root, builder.parentMap);
	}

	public IRNode parent(IRNode node) {
		if (node == root) {
			return null;
		}
		IRNode parent = parentMap.get(node);
		if (parent == null) {
			throw new NoSuchElementException("The node is not part of this tree.");
		}
		return parent;
	}

	public IRNode root() {
		return root;
	}

	private static class Builder implements Consumer<IRNode> {
		private Map<IRNode, IRNode> parentMap = new IdentityHashMap<>();
		private IRNode parent;
		@Override
		public void accept(IRNode node) {
			if (parentMap.containsKey(node) && parentMap.get(node) != parent) {
				throw new IllegalStateException("This node is already registered with another parent.");
			} else {
				parentMap.put(node, parent);
			}
			IRNode grandParent = parent;
			parent = node;
			node.forEachChild(this);
			parent = grandParent;
		}
	}
}
