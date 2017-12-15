package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public interface SourceUnit extends IRNode {
	enum InputLanguage {
		CAL, ORCC, XDF
	}

	NamespaceDecl getTree();
	SourceUnit withTree(NamespaceDecl ns);
	String getLocation();
	InputStream getInputStream() throws IOException;
	boolean isSynthetic();
	InputLanguage getLanguage();

	@Override
	default void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getTree());
	}

	@Override
	default SourceUnit transformChildren(Transformation transformation) {
		return withTree((NamespaceDecl) transformation.apply(getTree()));
	}
}
