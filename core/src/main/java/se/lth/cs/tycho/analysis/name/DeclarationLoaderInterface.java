package se.lth.cs.tycho.analysis.name;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.loader.DeclarationLoader;
import javarag.Inherited;

public interface DeclarationLoaderInterface {
	@Inherited
	public DeclarationLoader declarationLoader(IRNode node);
}
