package se.lth.cs.tycho.instance;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.function.Consumer;

public abstract class Instance extends AbstractIRNode implements PortContainer {
	
	protected final ImmutableList<PortDecl> inputPorts;
	protected final ImmutableList<PortDecl> outputPorts;

	public abstract <R, P> R accept(InstanceVisitor<R, P> visitor, P param);

	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}

	public Instance(IRNode original, List<PortDecl> inputPorts, List<PortDecl> outputPorts) {
		super(original);
		this.inputPorts = ImmutableList.from(inputPorts);
		this.outputPorts = ImmutableList.from(outputPorts);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		inputPorts.forEach(action);
		outputPorts.forEach(action);
	}
}
