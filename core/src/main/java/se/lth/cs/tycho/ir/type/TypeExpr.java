package se.lth.cs.tycho.ir.type;

import se.lth.cs.tycho.ir.IRNode;

public interface TypeExpr<This extends TypeExpr<This>> extends IRNode {
	@Override
	This transformChildren(Transformation transformation);
}
