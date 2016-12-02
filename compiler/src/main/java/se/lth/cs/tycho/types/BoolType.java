package se.lth.cs.tycho.types;

import se.lth.cs.tycho.ir.type.NominalTypeExpr;

public enum BoolType implements Type {
	INSTANCE;

	@Override
	public String toString() {
		return "bool";
	}
}
