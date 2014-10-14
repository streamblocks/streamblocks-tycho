package se.lth.cs.tycho.network.flatten.attr;

import java.util.Objects;

import se.lth.cs.tycho.network.flatten.attr.TreeRoot;

public final class TreeRoot<T> {
	private final T tree;

	public TreeRoot(T tree) {
		this.tree = tree;
	}

	public T getTree() {
		return tree;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TreeRoot && ((TreeRoot<?>) obj).tree.equals(tree);
	}
	
	@Override
	public int hashCode() {
		return tree.hashCode();
	}
	
	@Override
	public String toString() {
		return "[TreeRoot: " + Objects.toString(tree) + "]";
	}
	

}
