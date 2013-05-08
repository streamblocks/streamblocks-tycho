package net.opendf.ir.common;

import net.opendf.ir.util.ImmutableList;

public interface PortContainer {

	public ImmutableList<PortDecl> getInputPorts();
	
	public ImmutableList<PortDecl> getOutputPorts();
}
