package se.lth.cs.tycho.analysis.name;

import javarag.Inherited;
import javarag.Module;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;

public class NamespaceDecls extends Module<NamespaceDecls.Declarations> {
	public interface Declarations {
		@Inherited
		NamespaceDecl enclosingNamespaceDecl(IRNode node);
	}

	public NamespaceDecl enclosingNamespaceDecl(NamespaceDecl ns) {
		return ns;
	}
	
}
