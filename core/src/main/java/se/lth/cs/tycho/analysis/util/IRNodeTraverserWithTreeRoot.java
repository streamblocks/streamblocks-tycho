package se.lth.cs.tycho.analysis.util;

import java.util.Collections;

import se.lth.cs.tycho.ir.util.IRNodeTraverser;

public class IRNodeTraverserWithTreeRoot extends IRNodeTraverser {
	@Override
	public Iterable<? extends Object> getChildren(Object root) {
		if (root instanceof TreeRoot) {
			return Collections.emptyList();
		} else {
			return super.getChildren(root);
		}
	}
}