package se.lth.cs.tycho.network.flatten.attr;

import se.lth.cs.tycho.network.flatten.attr.Ports;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.PortDecl;
import javarag.Module;
import javarag.Synthesized;

public class Ports extends Module<Ports.Attributes> {

	public interface Attributes {
		@Synthesized
		PortDecl outputPortDecl(Object portContainer, Port port);

		@Synthesized
		PortDecl inputPortDecl(Object portContainer, Port port);
	}
	
	public PortDecl outputPortDecl(PortContainer portContainer, Port port) {
		for (PortDecl decl : portContainer.getOutputPorts()) {
			if (decl.getName().equals(port.getName())) {
				return decl;
			}
		}
		return null;
	}

	public PortDecl inputPortDecl(PortContainer portContainer, Port port) {
		for (PortDecl decl : portContainer.getInputPorts()) {
			if (decl.getName().equals(port.getName())) {
				return decl;
			}
		}
		return null;
	}
	
}
