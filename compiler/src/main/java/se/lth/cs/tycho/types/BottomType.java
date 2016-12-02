package se.lth.cs.tycho.types;

import se.lth.cs.tycho.ir.type.TypeExpr;

public enum BottomType implements Type {
	INSTANCE;

	@Override
	public String toString() {
		return "<bottom>";
	}
}
