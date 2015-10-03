package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;

public interface PortContainer extends IRNode {

	public ImmutableList<PortDecl> getInputPorts();
	
	public ImmutableList<PortDecl> getOutputPorts();
}
