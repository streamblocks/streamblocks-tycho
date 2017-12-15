package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class SyntheticSourceUnit implements SourceUnit {
	private final NamespaceDecl tree;
	private final int index;
	private static int nextIndex = 0;

	public SyntheticSourceUnit(NamespaceDecl tree) {
		this.tree = tree;
		this.index = nextIndex++;
	}

	@Override
	public NamespaceDecl getTree() {
		return tree;
	}

	@Override
	public SourceUnit withTree(NamespaceDecl tree) {
		return tree == this.tree ? this : new SyntheticSourceUnit(tree);
	}

	@Override
	public String getLocation() {
		return String.format("<synthetic-%d>", index);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSynthetic() {
		return true;
	}

	@Override
	public InputLanguage getLanguage() {
		return InputLanguage.CAL;
	}

	@Override
	public SyntheticSourceUnit clone() {
		try {
			return (SyntheticSourceUnit) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(tree);
	}

	@Override
	public SourceUnit transformChildren(Transformation transformation) {
		return withTree((NamespaceDecl) transformation.apply(tree));
	}
}
