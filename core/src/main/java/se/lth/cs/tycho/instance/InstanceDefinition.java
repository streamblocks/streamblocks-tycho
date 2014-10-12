package se.lth.cs.tycho.instance;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public abstract class InstanceDefinition extends Instance implements PortContainer {
	private final ImmutableList<PortDecl> inputPorts;
	private final ImmutableList<PortDecl> outputPorts;
	
	public InstanceDefinition(IRNode original, ImmutableList<PortDecl> inputPorts, ImmutableList<PortDecl> outputPorts) {
		super(original);
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
	}

	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}

}
