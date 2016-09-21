package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;

public interface Decl<This extends Decl<This>> extends IRNode {
	String getName();
	This withName(String name);
	String getOriginalName();

	@Override
	This clone();

	@Override
	This deepClone();

	@Override
	This transformChildren(Transformation transformation);

}
