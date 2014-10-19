package se.lth.cs.tycho.analysis.util;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.loader.DeclarationLoader;

public class TreeRoot {
	private final DeclarationLoader loader;
	private final IRNode mainTree;

	public TreeRoot(DeclarationLoader loader, IRNode mainTree) {
		this.loader = loader;
		this.mainTree = mainTree;
	}

	public DeclarationLoader getLoader() {
		return loader;
	}

	public IRNode getMainTree() {
		return mainTree;
	}

}
