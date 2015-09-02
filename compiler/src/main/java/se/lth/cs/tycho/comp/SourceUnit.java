package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.instance.net.ToolAttribute;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public interface SourceUnit extends IRNode {
	NamespaceDecl getTree();
	String getLocation();
	InputStream getInputStream() throws IOException;

	@Override
	default void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getTree());
	}
}
