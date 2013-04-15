package net.opendf.ir.common;

import java.util.List;

public interface PortContainer {

	public List<PortDecl> getInputPorts();
	
	public List<PortDecl> getOutputPorts();
}
