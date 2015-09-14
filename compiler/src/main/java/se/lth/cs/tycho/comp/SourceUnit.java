package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SourceUnit extends IRNode {
	enum InputLanguage {
		CAL, ORCC, XDF
	}

	NamespaceDecl getTree();
	SourceUnit withTree(NamespaceDecl ns);
	String getLocation();
	InputStream getInputStream() throws IOException;
	InputLanguage getLanguage();

	@Override
	default void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getTree());
	}

	@Override
	default SourceUnit transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		return withTree((NamespaceDecl) transformation.apply(getTree()));
	}
}
