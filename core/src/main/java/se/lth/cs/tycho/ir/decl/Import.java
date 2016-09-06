package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

public interface Import extends IRNode {
	Kind getKind();
	enum Kind { VAR, ENTITY, TYPE }
}
