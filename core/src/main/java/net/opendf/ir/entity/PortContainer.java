package net.opendf.ir.entity;

import net.opendf.ir.util.ImmutableList;

public interface PortContainer {

	public ImmutableList<PortDecl> getInputPorts();
	
	public ImmutableList<PortDecl> getOutputPorts();
}
