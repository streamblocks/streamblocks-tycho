package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.function.Consumer;

public abstract class Entity extends AbstractIRNode {

	protected final ImmutableList<PortDecl> inputPorts;
	protected final ImmutableList<PortDecl> outputPorts;
	protected final ImmutableList<TypeDecl> typeParameters;
	protected final ImmutableList<ParameterVarDecl> valueParameters;

	public Entity(IRNode original, List<PortDecl> inputPorts, List<PortDecl> outputPorts, List<TypeDecl> typeParameters, List<ParameterVarDecl> valueParameters) {
		super(original);
		this.inputPorts = ImmutableList.from(inputPorts);
		this.outputPorts = ImmutableList.from(outputPorts);
		this.valueParameters = ImmutableList.from(valueParameters);
		this.typeParameters = ImmutableList.from(typeParameters);
	}

	public ImmutableList<PortDecl> getInputPorts() {
		return inputPorts;
	}

	public Entity withInputPorts(List<PortDecl> inputPorts) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	public ImmutableList<PortDecl> getOutputPorts() {
		return outputPorts;
	}

	public Entity withOutputPorts(List<PortDecl> outputPorts) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	public ImmutableList<TypeDecl> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<ParameterVarDecl> getValueParameters() {
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
