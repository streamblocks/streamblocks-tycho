package se.lth.cs.tycho.ir;

public interface TypeExpr<This extends TypeExpr<This>> extends IRNode {
	@Override
	This transformChildren(Transformation transformation);
}
