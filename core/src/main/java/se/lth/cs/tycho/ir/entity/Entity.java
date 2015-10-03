package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.function.Consumer;

public abstract class Entity extends AbstractIRNode implements PortContainer {

	protected final ImmutableList<PortDecl> inputPorts;
	protected final ImmutableList<PortDecl> outputPorts;
	protected final ImmutableList<TypeDecl> typeParameters;
	protected final ImmutableList<VarDecl> valueParameters;

	public Entity(IRNode original, List<PortDecl> inputPorts, List<PortDecl> outputPorts,
			List<TypeDecl> typeParameters, List<VarDecl> valueParameters) {
		super(original);
		this.inputPorts = ImmutableList.from(inputPorts);
		this.outputPorts = ImmutableList.from(outputPorts);
		this.typeParameters = ImmutableList.from(typeParameters);
		this.valueParameters = ImmutableList.from(valueParameters);
	}

	public abstract <R, P> R accept(EntityVisitor<R, P> visitor, P param);

	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}

	public ImmutableList<TypeDecl> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<VarDecl> getValueParameters() {
		return valueParameters;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		inputPorts.forEach(action);
		outputPorts.forEach(action);
		typeParameters.forEach(action);
		valueParameters.forEach(action);
	}
}
