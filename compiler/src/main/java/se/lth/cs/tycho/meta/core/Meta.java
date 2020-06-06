package se.lth.cs.tycho.meta.core;

import se.lth.cs.tycho.ir.IRNode;

import java.util.List;

public interface Meta extends IRNode {

	List<MetaArgument> getArguments();
}
