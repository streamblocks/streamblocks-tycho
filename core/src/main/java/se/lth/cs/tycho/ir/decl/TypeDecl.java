package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

public class TypeDecl extends Decl {

	private TypeDecl(IRNode original, Availability availability, String name, DeclKind declKind,
			LocationKind locationKind) {
		super(original, locationKind, availability, declKind, name);
	}


}
