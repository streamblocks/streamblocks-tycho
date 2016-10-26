package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

public interface Decl extends IRNode {
	String getName();
	Decl withName(String name);
	String getOriginalName();

	@Override
	Decl transformChildren(Transformation transformation);

}
