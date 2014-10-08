package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.util.ImmutableList;

public interface PortContainer {

	public ImmutableList<PortDecl> getInputPorts();
	
	public ImmutableList<PortDecl> getOutputPorts();
}
