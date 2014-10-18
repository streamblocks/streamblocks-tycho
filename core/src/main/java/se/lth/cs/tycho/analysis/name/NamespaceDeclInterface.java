package se.lth.cs.tycho.analysis.name;

import javarag.Inherited;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;

public interface NamespaceDeclInterface {
	@Inherited
	public NamespaceDecl enclosingNamespaceDecl(IRNode node);
}
