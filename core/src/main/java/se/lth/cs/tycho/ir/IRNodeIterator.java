package se.lth.cs.tycho.ir;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;

class IRNodeIterator implements Iterator<IRNode> {
	private final ArrayDeque<IRNode> queue;
	private final ArrayDeque<IRNode> reverseNodes;

	public IRNodeIterator(IRNode root) {
		queue = new ArrayDeque<>();
		reverseNodes = new ArrayDeque<>();
		queue.add(root);
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public IRNode next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		IRNode next = queue.removeLast();
		assert reverseNodes.isEmpty();
		next.forEachChild(reverseNodes::addLast);
		while (!reverseNodes.isEmpty()) {
			queue.addLast(reverseNodes.removeLast());
		}
		return next;
	}
}
