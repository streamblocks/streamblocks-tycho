package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

public interface PortContainer {

	public CompositePortDecl getInputPorts();
	
	public CompositePortDecl getOutputPorts();
}
