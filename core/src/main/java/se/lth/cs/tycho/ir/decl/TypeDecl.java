package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public abstract class TypeDecl extends AbstractDecl {

	protected TypeDecl(TypeDecl original, String name) {
		super(original, name);
	}

}
