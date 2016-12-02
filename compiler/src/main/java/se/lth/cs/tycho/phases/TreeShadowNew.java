package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.ir.IRNode;

import java.util.IdentityHashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

public class TreeShadowNew implements TreeShadow {
	private final IRNode root;
	private final IdentityHashMap<IRNode, Tree<IRNode>> tree;

	private TreeShadowNew(IRNode root, IdentityHashMap<IRNode, Tree<IRNode>> tree) {
		this.root = root;
		this.tree = tree;
	}

	public static TreeShadowNew of(IRNode root) {
		IdentityHashMap<IRNode, Tree<IRNode>> tree = new IdentityHashMap<>();
		Tree.of(root).walk().forEach(treeNode -> {
			IRNode node = treeNode.node();
			if (tree.containsKey(node) && tree.get(node) != treeNode) {
				throw new IllegalStateException("This node is already registered.");
			} else {
				tree.put(node, treeNode);
			}
		});
		return new TreeShadowNew(root, tree);
	}

	@Override
	public IRNode parent(IRNode node) {
		Tree<IRNode> treeNode = tree.get(node);
		if (treeNode == null) {
			throw new NoSuchElementException("This node is not part of the tree.");
		} else {
			Optional<Tree<? extends IRNode>> optionalParent = treeNode.parent();
			if (optionalParent.isPresent()) {
				return optionalParent.get().node();
			} else {
				return null;
			}
		}
	}

	public <T extends IRNode> Tree<T> tree(T node) {
		Tree<IRNode> treeNode = tree.get(node);
		if (treeNode == null) {
			throw new NoSuchElementException("This node is nor part of the tree.");
		} else {
			return treeNode.assertNode(node);
		}
	}

	@Override
	public IRNode root() {
		return root;
	}
}
